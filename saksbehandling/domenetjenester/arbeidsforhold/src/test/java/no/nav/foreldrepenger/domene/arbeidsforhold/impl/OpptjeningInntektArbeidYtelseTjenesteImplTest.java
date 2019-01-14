package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.YtelseTestUtils.leggTilYtelse;
import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.YtelseTestUtils.opprettInntektArbeidYtelseAggregatForYrkesaktivitet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningAktivitetPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class OpptjeningInntektArbeidYtelseTjenesteImplTest {

    public static final String ORG_NUMMER = "21542512";
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final LocalDate skjæring = LocalDate.now();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
    private final AksjonspunktutlederForVurderOpptjening aksjonspunktutlederForVurderOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste tjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, aksjonspunktutlederForVurderOpptjening);
    private OpptjeningsperioderTjeneste asdf = new OpptjeningsperioderTjenesteImpl(tjeneste, repositoryProvider, resultatRepositoryProvider, aksjonspunktutlederForVurderOpptjening);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(tjeneste, resultatRepositoryProvider, asdf);
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private OpptjeningRepository opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
    private String AREBIDSFORHOLD_ID = "1";
    private AktørId AKTØRID = new AktørId("1");

    @Before
    public void setUp() throws Exception {
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(skjæring);
    }

    @Test
    public void skal_utlede_en_periode_for_egen_næring() {
        // Arrange
        final Behandling behandling = opprettBehandling(skjæring);

        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring.minusMonths(2));

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORG_NUMMER)
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now())
            .medOppstart(LocalDate.now())
            .oppdatertOpplysningerNå()
            .build();

        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
        virksomhetRepository.lagre(virksomhet);

        OppgittOpptjeningBuilder oppgitt = OppgittOpptjeningBuilder.ny();
        oppgitt.leggTilEgneNæringer(Collections.singletonList(OppgittOpptjeningBuilder.EgenNæringBuilder.ny()
            .medVirksomhet(virksomhet)
            .medPeriode(periode)
            .medRegnskapsførerNavn("Børre Larsen")
            .medRegnskapsførerTlf("TELEFON")
            .medBegrunnelse("Hva mer?")));

        inntektArbeidYtelseRepository.lagre(behandling, oppgitt);

        // Assert
        List<OpptjeningAktivitetPeriode> perioder = opptjeningTjeneste.hentRelevanteOpptjeningAktiveterForVilkårVurdering(behandling)
            .stream().filter(p -> p.getOpptjeningAktivitetType().equals(OpptjeningAktivitetType.NÆRING)).collect(Collectors.toList());

        assertThat(perioder).hasSize(1);
        OpptjeningAktivitetPeriode aktivitetPeriode = perioder.get(0);
        assertThat(aktivitetPeriode.getPeriode()).isEqualTo(periode);
        assertThat(aktivitetPeriode.getVurderingsStatus()).isEqualTo(VurderingsStatus.TIL_VURDERING);
    }

    @Test
    public void skal_sammenstille_grunnlag_og_overstyrt_deretter_utlede_opptjening_aktivitet_periode_for_vilkår_godkjent() {
        // Arrange
        final Behandling behandling = opprettBehandling(skjæring);

        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring.minusMonths(2));

        final Virksomhet virksomhet = opprettVirksomhet();
        InntektArbeidYtelseAggregatBuilder bekreftet = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            AKTØRID, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.REGISTER);
        tjeneste.lagre(behandling, bekreftet);

        // simulerer at det har blitt godkjent i GUI
        InntektArbeidYtelseAggregatBuilder saksbehandling = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            AKTØRID, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.SAKSBEHANDLET);
        tjeneste.lagre(behandling, saksbehandling);

        // Act
        List<OpptjeningAktivitetPeriode> perioder = opptjeningTjeneste.hentRelevanteOpptjeningAktiveterForVilkårVurdering(behandling);
        assertThat(perioder).hasSize(2);
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.FERDIG_VURDERT_GODKJENT)).collect(Collectors.toList()))
            .hasSize(1);
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.TIL_VURDERING)).collect(Collectors.toList())).hasSize(1);
    }

    @Test
    public void skal_sammenstille_grunnlag_og_overstyrt_deretter_utlede_opptjening_aktivitet_periode_for_vilkår_underkjent() {
        // Arrange
        LocalDate iDag = LocalDate.now();
        final Behandling behandling = opprettBehandling(iDag);

        DatoIntervallEntitet periode1 = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.minusMonths(3), iDag.minusMonths(2));

        final Arbeidsgiver virksomhet = Arbeidsgiver.virksomhet(opprettVirksomhet());
        InntektArbeidYtelseAggregatBuilder bekreftet = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(AKTØRID, AREBIDSFORHOLD_ID, periode1,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, virksomhet, VersjonType.REGISTER);
        tjeneste.lagre(behandling, bekreftet);

        // simulerer at det har blitt underkjent i GUI
        InntektArbeidYtelseAggregatBuilder overstyrt = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.of(bekreftet.build()), VersjonType.SAKSBEHANDLET);
        final String arbeidsforholdId = tjeneste.finnReferanseFor(behandling, virksomhet, ArbeidsforholdRef.ref(AREBIDSFORHOLD_ID), false).getReferanse();
        YrkesaktivitetBuilder yrkesaktivitetBuilder = overstyrt.getAktørArbeidBuilder(AKTØRID)
            .getYrkesaktivitetBuilderForNøkkelAvType(new Opptjeningsnøkkel(arbeidsforholdId, ORG_NUMMER, null),
                ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        yrkesaktivitetBuilder.tilbakestillAvtaler();
        tjeneste.lagre(behandling, overstyrt);

        // Act
        List<OpptjeningAktivitetPeriode> perioder = opptjeningTjeneste.hentRelevanteOpptjeningAktiveterForVilkårVurdering(behandling);
        assertThat(perioder).hasSize(2);
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.FERDIG_VURDERT_UNDERKJENT)).collect(Collectors.toList()))
            .hasSize(1);
        assertThat(perioder.stream().filter(p -> p.getVurderingsStatus().equals(VurderingsStatus.TIL_VURDERING)).collect(Collectors.toList())).hasSize(1);
    }

    @Test
    public void skal_hente_ytelse_før_stp() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);

        final Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring);

        final Virksomhet virksomhet = opprettVirksomhet();
        InntektArbeidYtelseAggregatBuilder builder = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            søkerAktørId, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.REGISTER);

        builder.leggTilAktørYtelse(leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(20), skjæring.minusDays(10),
            RelatertYtelseTilstand.AVSLUTTET, "12342234", RelatertYtelseType.FORELDREPENGER));
        builder.leggTilAktørYtelse(leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(3), skjæring.minusDays(1),
            RelatertYtelseTilstand.LØPENDE, "1222433", RelatertYtelseType.SYKEPENGER));

        inntektArbeidYtelseRepository.lagre(behandling, builder);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, skjæring.minusDays(30), skjæring);

        // Act
        Optional<Ytelse> sisteYtelseOpt = opptjeningTjeneste.hentSisteInfotrygdYtelseFørSkjæringstidspunktForOpptjening(behandling);

        // Assert
        assertThat(sisteYtelseOpt.isPresent()).isTrue();
        assertThat(sisteYtelseOpt.get().getSaksnummer().getVerdi()).isEqualTo("1222433");
    }

    @Test
    public void skal_lage_sammehengende_liste() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);

        final Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring);

        final Virksomhet virksomhet = opprettVirksomhet();
        InntektArbeidYtelseAggregatBuilder builder = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            søkerAktørId, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.REGISTER);

        builder.leggTilAktørYtelse(leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(10), skjæring.minusDays(2),
            RelatertYtelseTilstand.LØPENDE, "12342234", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(15), skjæring.minusDays(11),
            RelatertYtelseTilstand.AVSLUTTET, "1222433", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(20), skjæring.minusDays(16),
            RelatertYtelseTilstand.AVSLUTTET, "124234", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(30), skjæring.minusDays(22),
            RelatertYtelseTilstand.AVSLUTTET, "123253254", RelatertYtelseType.SYKEPENGER));

        inntektArbeidYtelseRepository.lagre(behandling, builder);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, skjæring.minusDays(30), skjæring);

        // Act
        Collection<Ytelse> sammenhengendeYtelser = opptjeningTjeneste.hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);

        // Assert
        assertThat(sammenhengendeYtelser).hasSize(3);
        assertThat(sammenhengendeYtelser.stream().map(s -> s.getSaksnummer().getVerdi()).collect(Collectors.toList())).containsOnly("12342234", "1222433",
            "124234");
    }

    @Test
    public void skal_lage_sammehengende_liste_med_overlappende_ytelser() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);

        final Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring);

        final Virksomhet virksomhet = opprettVirksomhet();
        InntektArbeidYtelseAggregatBuilder builder = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            søkerAktørId, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.REGISTER);

        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(30), skjæring.plusDays(10), RelatertYtelseTilstand.LØPENDE, "12342234", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(29), skjæring.minusDays(20), RelatertYtelseTilstand.AVSLUTTET, "1222433", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(19), skjæring.minusDays(10), RelatertYtelseTilstand.AVSLUTTET, "124234", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(9), skjæring.minusDays(1), RelatertYtelseTilstand.AVSLUTTET, "123253254", RelatertYtelseType.SYKEPENGER));

        inntektArbeidYtelseRepository.lagre(behandling, builder);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, skjæring.minusDays(30), skjæring);

        // Act
        Collection<Ytelse> sammenhengendeYtelser = opptjeningTjeneste.hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);

        // Assert
        assertThat(sammenhengendeYtelser).hasSize(4);
        assertThat(sammenhengendeYtelser.stream().map(s -> s.getSaksnummer().getVerdi()).collect(Collectors.toList())).containsOnly("12342234", "1222433",
            "124234", "123253254");
    }

    @Test
    public void skal_lage_sammehengende_liste_med_delvis_overlappende_ytelser() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);
        final Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring);

        final Virksomhet virksomhet = opprettVirksomhet();
        InntektArbeidYtelseAggregatBuilder builder = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            søkerAktørId, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.REGISTER);

        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(10), skjæring.plusDays(10), RelatertYtelseTilstand.LØPENDE, "12342234", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(20), skjæring.minusDays(10), RelatertYtelseTilstand.AVSLUTTET, "1222433", RelatertYtelseType.SYKEPENGER));

        inntektArbeidYtelseRepository.lagre(behandling, builder);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, skjæring.minusDays(30), skjæring);

        // Act
        Collection<Ytelse> sammenhengendeYtelser = opptjeningTjeneste.hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);

        // Assert
        assertThat(sammenhengendeYtelser).hasSize(2);
        assertThat(sammenhengendeYtelser.stream().map(s -> s.getSaksnummer().getVerdi()).collect(Collectors.toList())).containsOnly("12342234", "1222433");
    }

    @Test
    public void skal_ikke_lage_sammehengende_liste_med_1_dag_mellom_ytelser() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);
        final Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring);

        final Virksomhet virksomhet = opprettVirksomhet();
        InntektArbeidYtelseAggregatBuilder builder = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            søkerAktørId, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.REGISTER);

        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(10), skjæring.plusDays(10), RelatertYtelseTilstand.LØPENDE, "12342234", RelatertYtelseType.SYKEPENGER));
        builder.leggTilAktørYtelse(
            leggTilYtelse(builder.getAktørYtelseBuilder(søkerAktørId), skjæring.minusDays(20), skjæring.minusDays(12), RelatertYtelseTilstand.AVSLUTTET, "1222433", RelatertYtelseType.SYKEPENGER));

        tjeneste.lagre(behandling, builder);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, skjæring.minusDays(30), skjæring);

        // Act
        Collection<Ytelse> sammenhengendeYtelser = opptjeningTjeneste.hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);

        // Assert
        assertThat(sammenhengendeYtelser).hasSize(1);
        assertThat(sammenhengendeYtelser.stream().map(s -> s.getSaksnummer().getVerdi()).collect(Collectors.toList())).containsOnly("12342234");
    }

    @Test
    public void skal_lage_tom_sammehengende_liste_uten_ytelser() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = scenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId)
            .build();

        scenario.medRegisterOpplysninger(personInformasjon);
        final Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(skjæring.minusMonths(3), skjæring);

        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder
            .oppdatere(Optional.empty(), VersjonType.SAKSBEHANDLET);

        final Virksomhet virksomhet = opprettVirksomhet();
        InntektArbeidYtelseAggregatBuilder bekreftet = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            søkerAktørId, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.REGISTER);
        tjeneste.lagre(behandling, bekreftet);

        // simulerer at det har blitt godkjent i GUI
        InntektArbeidYtelseAggregatBuilder saksbehandling = opprettInntektArbeidYtelseAggregatForYrkesaktivitet(
            søkerAktørId, AREBIDSFORHOLD_ID, periode, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, BigDecimal.ZERO, Arbeidsgiver.virksomhet(virksomhet),
            VersjonType.SAKSBEHANDLET);
        tjeneste.lagre(behandling, saksbehandling);

        inntektArbeidYtelseRepository.lagre(behandling, builder);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, skjæring.minusDays(30), skjæring);

        // Act
        Collection<Ytelse> sammenhengendeYtelser = opptjeningTjeneste.hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(behandling);

        // Assert
        assertThat(sammenhengendeYtelser).hasSize(0);
    }

    private Virksomhet opprettVirksomhet() {
        VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();

        final Optional<Virksomhet> hent = virksomhetRepository.hent(ORG_NUMMER);
        if (hent.isPresent()) {
            return hent.get();
        }

        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ORG_NUMMER)
            .medNavn("Virksomheten")
            .medRegistrert(LocalDate.now().minusYears(2L))
            .medOppstart(LocalDate.now().minusYears(1L))
            .oppdatertOpplysningerNå()
            .build();

        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
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
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
        final VilkårResultat nyttResultat = VilkårResultat.builder().buildFor(behandlingsresultat);
        behandlingRepository.lagre(nyttResultat, lås);
        behandlingRepository.lagre(behandlingsresultat, lås);

        resultatRepositoryProvider.getOpptjeningRepository().lagreOpptjeningsperiode(behandlingsresultat, skjæring.minusMonths(10), skjæring);
        return behandling;
    }
}
