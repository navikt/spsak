package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningsperiodeForSaksbehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class OpptjeningsperioderTjenesteImplTest {

    public static final String ORG_NUMMER = "21542512";

    public static final String ORG_NUMMER_UTENLANDSK = "21542513";
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private final AksjonspunktutlederForVurderOpptjening aksjonspunktutlederForVurderOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste tjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null,
        skjæringstidspunktTjeneste, aksjonspunktutlederForVurderOpptjening);
    private OpptjeningsperioderTjeneste forSaksbehandlingTjeneste = new OpptjeningsperioderTjenesteImpl(tjeneste, repositoryProvider, resultatRepositoryProvider, aksjonspunktutlederForVurderOpptjening);
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();

    private String AREBIDSFORHOLD_ID = "1";
    private AktørId AKTØRID = new AktørId("1");
    private final LocalDate skjæringstidspunkt = LocalDate.now();

    @Before
    public void setUp() throws Exception {
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(skjæringstidspunkt);
    }

    @Test
    @Ignore("TODO (Diamant) Fiks feilende test. Startet å feile ved datoovergang 1. november")
    public void skal_sjekk_scenario_med_utlandsk_arbeid() {
        //Arrange
        final Behandling behandling = opprettBehandling(skjæringstidspunkt);

        DatoIntervallEntitet periode1 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(11), skjæringstidspunkt.minusMonths(6));
        final Arbeidsgiver utenlandskVirksomhet = Arbeidsgiver.virksomhet(opprettUtenlandskVirksomhet());

        OppgittOpptjeningBuilder oppgittUtenlandsk = OppgittOpptjeningBuilder.ny();

        //UTENLANDSK
        oppgittUtenlandsk.leggTilOppgittArbeidsforhold(
            OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder.ny()
                .medPeriode(periode1)
                .medErUtenlandskInntekt(true)
                .medVirksomhet(utenlandskVirksomhet.getVirksomhet())
                .medArbeidType(ArbeidType.UTENLANDSK_ARBEIDSFORHOLD)
        );
        inntektArbeidYtelseRepository.lagre(behandling, oppgittUtenlandsk);

        String begrunnelse = "Det Ser greit ut";
        InntektArbeidYtelseAggregatBuilder bekreftet1 = opprettInntektArbeidYtelseAggregatMedArbeidType(AKTØRID, AREBIDSFORHOLD_ID, periode1,
            utenlandskVirksomhet, ArbeidType.UTENLANDSK_ARBEIDSFORHOLD, begrunnelse, VersjonType.SAKSBEHANDLET);
        tjeneste.lagre(behandling, bekreftet1);

        //Act
        List<OpptjeningsperiodeForSaksbehandling> perioder = forSaksbehandlingTjeneste.hentRelevanteOpptjeningAktiveterForSaksbehandling(behandling);

        //Assert
        assertThat(perioder).hasSize(1);
        OpptjeningsperiodeForSaksbehandling opptjeningsperiode = perioder.stream().filter(p -> p.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD)).findFirst().get();
        assertThat(opptjeningsperiode.getBegrunnelse()).isEqualTo(begrunnelse);
    }

    @Test
    public void skal_sammenstille_grunnlag_og_overstyrt_deretter_utlede_opptjening_aktivitet_periode() {
        //Arrange
        final Behandling behandling = opprettBehandling(skjæringstidspunkt);

        DatoIntervallEntitet periode1 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(3), skjæringstidspunkt.minusMonths(2));
        DatoIntervallEntitet periode2 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(2), skjæringstidspunkt.minusMonths(1));
        DatoIntervallEntitet periode3 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(1), skjæringstidspunkt.minusMonths(0));

        OppgittOpptjeningBuilder oppgitt = OppgittOpptjeningBuilder.ny();
        oppgitt.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode2, ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        inntektArbeidYtelseRepository.lagre(behandling, oppgitt);

        final Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(opprettVirksomhet());
        InntektArbeidYtelseAggregatBuilder bekreftet = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, periode1, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.TEN, virksomhet);
        tjeneste.lagre(behandling, bekreftet);

        InntektArbeidYtelseAggregatBuilder saksbehandlet = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.of(bekreftet.build()), VersjonType.SAKSBEHANDLET);
        final String arbeidsforholdId = tjeneste.finnReferanseFor(behandling, virksomhet, ArbeidsforholdRef.ref(AREBIDSFORHOLD_ID), false).getReferanse();
        YrkesaktivitetBuilder yrkesaktivitetBuilder = saksbehandlet.getAktørArbeidBuilder(AKTØRID)
            .getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(arbeidsforholdId, null, null),
                ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilder
            .leggTilAktivitetsAvtale(yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(periode3.getFomDato(), periode3.getTomDato()))
                .medProsentsats(BigDecimal.TEN)
                .medAntallTimer(BigDecimal.valueOf(20.4d))
                .medAntallTimerFulltid(BigDecimal.valueOf(10.2d)));

        tjeneste.lagre(behandling, saksbehandlet);

        //Act
        List<OpptjeningsperiodeForSaksbehandling> perioder = forSaksbehandlingTjeneste.hentRelevanteOpptjeningAktiveterForSaksbehandling(behandling);

        //Assert
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.GODKJENT)).collect(Collectors.toList())).hasSize(2);
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.UNDERKJENT)).collect(Collectors.toList())).hasSize(1);
    }

    @Test
    public void skal_sammenstille_grunnlag_og_utlede_opptjening_aktivitet_periode() {
        //Arrange
        Behandling behandling = opprettBehandling(skjæringstidspunkt);

        DatoIntervallEntitet periode1 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(3), skjæringstidspunkt.minusMonths(2));
        DatoIntervallEntitet periode2 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(2), skjæringstidspunkt.minusMonths(1));

        OppgittOpptjeningBuilder oppgitt = OppgittOpptjeningBuilder.ny();
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORG_NUMMER)
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        virksomhetRepository.lagre(virksomhet);

        OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny();
        egenNæringBuilder
            .medRegnskapsførerNavn("Larsen")
            .medRegnskapsførerTlf("TELEFON")
            .medVirksomhet(virksomhet)
            .medPeriode(periode2);

        oppgitt.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode2, ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        oppgitt.leggTilEgneNæringer(Arrays.asList(egenNæringBuilder));
        inntektArbeidYtelseRepository.lagre(behandling, oppgitt);

        InntektArbeidYtelseAggregatBuilder bekreftet = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, periode1, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.TEN, Arbeidsgiver.virksomhet(virksomhet));
        tjeneste.lagre(behandling, bekreftet);

        //Act
        List<OpptjeningsperiodeForSaksbehandling> perioder = forSaksbehandlingTjeneste.hentRelevanteOpptjeningAktiveterForSaksbehandling(behandling);

        //Assert
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.GODKJENT)).collect(Collectors.toList())).hasSize(3);
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.TIL_VURDERING)).collect(Collectors.toList())).hasSize(1);
        assertThat(perioder.stream().filter(OpptjeningsperiodeForSaksbehandling::getErManueltRegistrert).collect(Collectors.toList())).isEmpty();
        assertThat(perioder.stream().filter(o -> !o.getErManueltRegistrert()).collect(Collectors.toList())).hasSize(4);
    }

    @Test
    public void skal_utlede_om_en_periode_er_blitt_endret() {
        //Arrange
        final Behandling behandling = opprettBehandling(skjæringstidspunkt);

        DatoIntervallEntitet periode1 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(3), skjæringstidspunkt.minusMonths(2));
        DatoIntervallEntitet periode2 = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt.minusMonths(2), skjæringstidspunkt.minusMonths(1));

        OppgittOpptjeningBuilder oppgitt = OppgittOpptjeningBuilder.ny();
        oppgitt.leggTilAnnenAktivitet(new AnnenAktivitetEntitet(periode2, ArbeidType.MILITÆR_ELLER_SIVILTJENESTE));
        inntektArbeidYtelseRepository.lagre(behandling, oppgitt);

        InntektArbeidYtelseAggregatBuilder saksbehandlet = opprettOverstyrtOppgittOpptjening(periode1,
            ArbeidType.MILITÆR_ELLER_SIVILTJENESTE, AKTØRID, VersjonType.SAKSBEHANDLET);
        tjeneste.lagre(behandling, saksbehandlet);

        //Act
        //Assert
        List<OpptjeningsperiodeForSaksbehandling> perioder = forSaksbehandlingTjeneste.hentRelevanteOpptjeningAktiveterForSaksbehandling(behandling)
            .stream().filter(p -> p.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE)).collect(Collectors.toList());

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getErPeriodeEndret()).isTrue();
        assertThat(perioder.get(0).getBegrunnelse()).isNotEmpty();
    }

    private InntektArbeidYtelseAggregatBuilder opprettOverstyrtOppgittOpptjening(DatoIntervallEntitet periode, ArbeidType type, AktørId aktørId, VersjonType register) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), register);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForType(type);

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(periode)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d))
            .medBeskrivelse("Ser greit ut");

        yrkesaktivitetBuilder
            .medArbeidType(type)
            .leggTilAktivitetsAvtale(aktivitetsAvtale);
        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        return builder;
    }

    private Behandling opprettBehandling(LocalDate skjæringstidspunkt) {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(AKTØRID)
            .medFødselsdato(skjæringstidspunkt.minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12312312312"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        @SuppressWarnings("unused")
        Long fagsakId = fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        return behandling;
    }

    private Virksomhet opprettVirksomhet() {
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORG_NUMMER)
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private Virksomhet opprettUtenlandskVirksomhet() {

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORG_NUMMER_UTENLANDSK)
            .medNavn("Utenlandsk Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private InntektArbeidYtelseAggregatBuilder opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AktørId aktørId, String arbeidsforhold,
                                                                                                   DatoIntervallEntitet periode, ArbeidType type,
                                                                                                   BigDecimal prosentsats, Arbeidsgiver virksomhet1) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(
            new Opptjeningsnøkkel(arbeidsforhold, virksomhet1.getIdentifikator(), null), type);

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(periode)
            .medProsentsats(prosentsats)
            .medAntallTimer(BigDecimal.valueOf(20.4d))
            .medAntallTimerFulltid(BigDecimal.valueOf(10.2d))
            .medBeskrivelse("Ser greit ut");
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder ansettelsesperiode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(periode);

        Permisjon permisjon = permisjonBuilder
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.UTDANNINGSPERMISJON)
            .medPeriode(periode.getFomDato(), periode.getTomDato())
            .medProsentsats(BigDecimal.valueOf(100))
            .build();

        yrkesaktivitetBuilder
            .medArbeidType(type)
            .medArbeidsgiver(virksomhet1)
            .medArbeidsforholdId(ArbeidsforholdRef.ref(AREBIDSFORHOLD_ID))
            .leggTilPermisjon(permisjon)
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilAktivitetsAvtale(ansettelsesperiode);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        return builder;
    }

    private InntektArbeidYtelseAggregatBuilder opprettInntektArbeidYtelseAggregatMedArbeidType(AktørId aktørId, String arbeidsforhold,
                                                                                               DatoIntervallEntitet periode, Arbeidsgiver virksomhet1, ArbeidType arbeidType, String begrunnelse, VersjonType register) {
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), register);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder = builder.getAktørArbeidBuilder(aktørId);
        YrkesaktivitetBuilder yrkesaktivitetBuilder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(
            new Opptjeningsnøkkel(arbeidsforhold, virksomhet1.getIdentifikator(), null), arbeidType);

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder();

        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtale = aktivitetsAvtaleBuilder
            .medPeriode(periode)
            .medProsentsats(BigDecimal.valueOf(100.0d))
            .medAntallTimer(BigDecimal.valueOf(40.0d))
            .medAntallTimerFulltid(BigDecimal.valueOf(40.0d))
            .medBeskrivelse(begrunnelse);
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder ansettelsesperiode = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder()
            .medPeriode(periode);

        yrkesaktivitetBuilder
            .medArbeidType(arbeidType)
            .medArbeidsgiver(virksomhet1)
            .medArbeidsforholdId(ArbeidsforholdRef.ref(arbeidsforhold))
            .leggTilAktivitetsAvtale(aktivitetsAvtale)
            .leggTilAktivitetsAvtale(ansettelsesperiode);

        InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeid = aktørArbeidBuilder
            .leggTilYrkesaktivitet(yrkesaktivitetBuilder);

        builder.leggTilAktørArbeid(aktørArbeid);

        return builder;
    }

}
