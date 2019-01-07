package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.aksjonspunkt.BekreftOpptjeningPeriodeDto;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BekreftOpptjeningPeriodeAksjonspunktTest {
    private final SkjæringstidspunktTjeneste mock = mock(SkjæringstidspunktTjeneste.class);
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepository behandlingRepository = new BehandlingRepositoryImpl(repoRule.getEntityManager());
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());
    private VirksomhetRepository virksomhetRepository = new VirksomhetRepositoryImpl(repoRule.getEntityManager());
    private VirksomhetTjeneste tjeneste;

    private BekreftOpptjeningPeriodeAksjonspunkt bekreftOpptjeningPeriodeAksjonspunkt;

    private AktørId AKTØRID = new AktørId("1");
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private final AksjonspunktutlederForVurderOpptjening vurderOpptjening = mock(AksjonspunktutlederForVurderOpptjening.class);


    @Before
    public void oppsett() {
        tjeneste = mock(VirksomhetTjeneste.class);
        VirksomhetEntitet.Builder builder = new VirksomhetEntitet.Builder();
        VirksomhetEntitet børreAs = builder.medOrgnr("23948923849283")
            .oppdatertOpplysningerNå()
            .medNavn("Børre AS")
            .build();
        virksomhetRepository.lagre(børreAs);
        Mockito.when(tjeneste.finnOrganisasjon(Mockito.any())).thenReturn(Optional.of(børreAs));
        iayTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, mock, vurderOpptjening);
        bekreftOpptjeningPeriodeAksjonspunkt = new BekreftOpptjeningPeriodeAksjonspunkt(repositoryProvider, tjeneste, iayTjeneste, vurderOpptjening);
    }

    @Test
    public void skal_lagre_ned_bekrefet_aksjonspunkt() {
        LocalDate iDag = LocalDate.now();
        final Behandling behandling = opprettBehandling(iDag);

        DatoIntervallEntitet periode1 = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.minusMonths(3), iDag.minusMonths(2));
        DatoIntervallEntitet periode1_2 = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.minusMonths(2), iDag.minusMonths(1));

        OppgittOpptjeningBuilder oppgitt = OppgittOpptjeningBuilder.ny();
        oppgitt.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode1, ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        oppgitt.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode1, ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        oppgitt.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode1_2, ArbeidType.SLUTTPAKKE));
        inntektArbeidYtelseRepository.lagre(behandling, oppgitt);

        // simulerer svar fra GUI
        DatoIntervallEntitet periode2 = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.minusMonths(2), iDag.minusMonths(1));
        BekreftOpptjeningPeriodeDto dto = new BekreftOpptjeningPeriodeDto();
        dto.setAktivitetType(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE);
        dto.setOriginalTom(periode1.getTomDato());
        dto.setOriginalFom(periode1.getFomDato());
        dto.setOpptjeningFom(periode2.getFomDato());
        dto.setOpptjeningTom(periode2.getTomDato());
        dto.setErGodkjent(true);
        dto.setErEndret(true);
        dto.setBegrunnelse("Ser greit ut");
        BekreftOpptjeningPeriodeDto dto2 = new BekreftOpptjeningPeriodeDto();
        dto2.setAktivitetType(OpptjeningAktivitetType.SLUTTPAKKE);
        dto2.setOpptjeningFom(periode1_2.getFomDato());
        dto2.setOpptjeningTom(periode1_2.getTomDato());
        dto2.setOriginalFom(periode1_2.getFomDato());
        dto2.setOriginalTom(periode1_2.getTomDato());
        dto2.setErGodkjent(false);
        dto2.setBegrunnelse("Ser greit ut");
        dto2.setOppdragsgiverOrg("test");
        dto2.setArbeidsgiver("test");
        BekreftOpptjeningPeriodeDto dto3 = new BekreftOpptjeningPeriodeDto();
        dto3.setAktivitetType(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE);
        dto3.setOriginalTom(periode1.getTomDato());
        dto3.setOriginalFom(periode1.getFomDato());
        dto3.setOpptjeningFom(periode1.getFomDato());
        dto3.setOpptjeningTom(periode1.getTomDato());
        dto3.setErGodkjent(true);
        dto3.setBegrunnelse("Ser greit ut");

        //Act
        bekreftOpptjeningPeriodeAksjonspunkt.oppdater(behandling, asList(dto, dto2, dto3));

        InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);
        assertThat(grunnlag.getSaksbehandletVersjon()).isPresent();
        AktørArbeid aktørArbeid = grunnlag.getSaksbehandletVersjon().get().getAktørArbeid().iterator().next();
        assertThat(aktørArbeid.getYrkesaktiviteter()).hasSize(1);
        final List<DatoIntervallEntitet> perioder = aktørArbeid.getYrkesaktiviteter().iterator().next().getAktivitetsAvtaler().stream().map(AktivitetsAvtale::getPeriode).collect(Collectors.toList());
        assertThat(perioder).contains(periode1, periode2);
    }

    @Test
    public void skal_lagre_endring_i_periode_for_egen_næring() {
        LocalDate iDag = LocalDate.now();
        final Behandling behandling = opprettBehandling(iDag);

        when(vurderOpptjening.girAksjonspunktForOppgittNæring(any())).thenReturn(true);
        DatoIntervallEntitet periode1 = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.minusMonths(3), iDag.minusMonths(2));
        DatoIntervallEntitet periode1_2 = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.minusMonths(2), iDag.minusMonths(2));

        OppgittOpptjeningBuilder oppgitt = OppgittOpptjeningBuilder.ny();
        oppgitt.leggTilEgneNæringer(asList(OppgittOpptjeningBuilder.EgenNæringBuilder.ny()
            .medPeriode(periode1)));
        inntektArbeidYtelseRepository.lagre(behandling, oppgitt);

        BekreftOpptjeningPeriodeDto dto = new BekreftOpptjeningPeriodeDto();
        dto.setAktivitetType(OpptjeningAktivitetType.NÆRING);
        dto.setOriginalTom(periode1.getTomDato());
        dto.setOriginalFom(periode1.getFomDato());
        dto.setOpptjeningFom(periode1_2.getFomDato());
        dto.setOpptjeningTom(periode1_2.getTomDato());
        dto.setErGodkjent(true);
        dto.setErEndret(true);
        dto.setBegrunnelse("Ser greit ut");

        //Act
        bekreftOpptjeningPeriodeAksjonspunkt.oppdater(behandling, asList(dto));
        InntektArbeidYtelseGrunnlag grunnlag = inntektArbeidYtelseRepository.hentAggregat(behandling, null);
        assertThat(grunnlag.getSaksbehandletVersjon()).isPresent();
        AktørArbeid aktørArbeid = grunnlag.getSaksbehandletVersjon().get().getAktørArbeid().iterator().next();
        assertThat(aktørArbeid.getYrkesaktiviteter()).hasSize(1);
        AktivitetsAvtale aktivitetsAvtale = aktørArbeid.getYrkesaktiviteter().iterator().next().getAktivitetsAvtaler().iterator().next();
        assertThat(DatoIntervallEntitet.fraOgMedTilOgMed(aktivitetsAvtale.getFraOgMed(), aktivitetsAvtale.getTilOgMed())).isEqualTo(periode1_2);
    }

    private Behandling opprettBehandling(LocalDate iDag) {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(iDag.minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        return behandling;
    }
}
