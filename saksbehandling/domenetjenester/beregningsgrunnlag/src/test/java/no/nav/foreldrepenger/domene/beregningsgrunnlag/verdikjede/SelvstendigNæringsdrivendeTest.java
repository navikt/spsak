package no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGSNetterAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGSNførAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserPeriode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.domene.arbeidsforhold.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class SelvstendigNæringsdrivendeTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String DUMMY_ORGNR = "999";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;

    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjeneste(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningRepository opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;

    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjeneste(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, periodeTjeneste);

    private ScenarioMorSøkerForeldrepenger scenario;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;
    private BehandlingRepository behandlingRepository;

    @Before
    public void setup() {
        if (fastsettSkjæringstidspunktOgStatuser != null) {
            return;
        }
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingTjeneste.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjeneste(resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        fastsettSkjæringstidspunktOgStatuser = new FastsettSkjæringstidspunktOgStatuser(oversetterTilRegel, oversetterFraRegel);
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjeneste(repositoryProvider, inntektArbeidYtelseTjeneste);
        KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        beregningsperiodeTjeneste = new BeregningsperiodeTjeneste(inntektArbeidYtelseTjeneste, beregningsgrunnlagRepository, 5);
        KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilaserTjeneste = new KontrollerFaktaBeregningFrilanserTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste);
        faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(resultatRepositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilaserTjeneste);
        aksjonspunktUtlederForBeregning = new AksjonspunktUtlederForBeregning(repositoryProvider.getAksjonspunktRepository(), faktaOmBeregningTilfelleTjeneste, beregningsperiodeTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(behandlingRepository, oversetterTilRegel, oversetterFraRegel);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        this.fastsettBeregningsgrunnlagPeriodeTjeneste = new FastsettBeregningsgrunnlagPerioderTjeneste(inntektArbeidYtelseTjeneste, beregningInntektsmeldingTjeneste);
    }

    public Behandling lagBehandlingATogFLogSN(ScenarioMorSøkerForeldrepenger scenario,
                                              List<BigDecimal> inntektBeregningsgrunnlag,
                                              BigDecimal inntektFrilans, List<BigDecimal> årsinntekterSN, int førsteÅr, BigDecimal årsinntektVarigEndring,
                                              VirksomhetEntitet... virksomheter) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();

        VirksomhetEntitet dummyVirksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(DUMMY_ORGNR)
            .medNavn("Dummyvirksomhet")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(dummyVirksomhet);

        VerdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, fraOgMed, tilOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER, dummyVirksomhet);
        VerdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, virksomheter);

        List<DatoIntervallEntitet> perioder = VerdikjedeTestHjelper.utledPerioderMellomFomTom(fraOgMed, tilOgMed);

        VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder,
            AKTØR_ID, perioder,
            inntektBeregningsgrunnlag,
            virksomheter);
        if (inntektFrilans != null) {
            VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, AKTØR_ID, perioder, List.of(inntektFrilans), dummyVirksomhet);
        }

        if (årsinntekterSN != null) {
            for (int ix = 0; ix < 3; ix++) {
                VerdikjedeTestHjelper.lagInntektForSN(inntektArbeidYtelseBuilder, AKTØR_ID, LocalDate.of(førsteÅr + ix, Month.JANUARY, 1), årsinntekterSN.get(ix));
            }
        }
        if (årsinntektVarigEndring != null) {
            OppgittOpptjeningBuilder.EgenNæringBuilder egenNæringBuilder = OppgittOpptjeningBuilder.EgenNæringBuilder.ny()
                .medBruttoInntekt(årsinntektVarigEndring)
                .medVarigEndring(true)
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(1), SKJÆRINGSTIDSPUNKT_BEREGNING))
                .medEndringDato(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(1));
            scenario.medOppgittOpptjening(OppgittOpptjeningBuilder.ny()
                .leggTilEgneNæringer(Arrays.asList(egenNæringBuilder)));
        }
        return scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Test
    public void skalBeregneAvvikVedVarigEndring() {
        // Arrange
        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        final List<Double> ÅRSINNTEKT = Arrays.asList(7.0 * GrunnbeløpForTest.GSNITT_2014, 8.0 * GrunnbeløpForTest.GSNITT_2015, 9.0 * GrunnbeløpForTest.GSNITT_2016);

        final double varigEndringMånedsinntekt = 8.0 * GrunnbeløpForTest.GRUNNBELØP_2017 / 12;
        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 624226.66666;
        final Long forventetAvvikPromille = 200L;
        final double forventetAvkortet = 6.0 * GrunnbeløpForTest.GRUNNBELØP_2017;
        final double forventetRedusert = forventetAvkortet;

        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        Behandling behandling = lagBehandlingATogFLogSN(
            scenario, Arrays.asList(), null, årsinntekterSN, 2014, BigDecimal.valueOf(12 * varigEndringMånedsinntekt));

//        VerdikjedeTestHjelper.settoppÅrsinntekterVarigEndring(beregningsgrunnlagMockRepository, ÅRSINNTEKT, ÅR, varigEndringMånedsinntekt);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGSNførAvkorting(periode, forventetBrutto, 2016);
        Sammenligningsgrunnlag sg = foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag();
        assertThat(sg).isNotNull();
        assertThat(sg.getAvvikPromille()).isEqualTo(forventetAvvikPromille);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullføreBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }

    @Test
    public void sammeLønnHvertÅrUnder6GIkkeVarigEndring() {

        //PGI <= 6xGsnitt: Bidrag til beregningsgrunnlaget = PGI/Gsnitt
        final double årsinntekt1 = 4.0 * GrunnbeløpForTest.GSNITT_2014;
        final double årsinntekt2 = 4.0 * GrunnbeløpForTest.GSNITT_2015;
        final double årsinntekt3 = 4.0 * GrunnbeløpForTest.GSNITT_2016;
        final List<Double> ÅRSINNTEKT = Arrays.asList(årsinntekt1, årsinntekt2, årsinntekt3);

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 4.0 * GrunnbeløpForTest.GRUNNBELØP_2017;
        final double forventetAvkortet = forventetBrutto;
        final double forventetRedusert = forventetAvkortet;
        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        Behandling behandling = lagBehandlingATogFLogSN(
            scenario, Arrays.asList(), null, årsinntekterSN, 2014, null);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGSNførAvkorting(periode, forventetBrutto, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fastsattBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fastsattBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        periode = fastsattBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }

    @Test
    public void sammeLønnHvertÅrOver6GIkkeVarigEndring() {

        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        final double årsinntekt1 = 7.0 * GrunnbeløpForTest.GSNITT_2014;
        final double årsinntekt2 = 7.0 * GrunnbeløpForTest.GSNITT_2015;
        final double årsinntekt3 = 7.0 * GrunnbeløpForTest.GSNITT_2016;
        final List<Double> ÅRSINNTEKT = Arrays.asList(årsinntekt1, årsinntekt2, årsinntekt3);

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 6.333333 * GrunnbeløpForTest.GRUNNBELØP_2017;
        final double forventetAvkortet = 6.0 * GrunnbeløpForTest.GRUNNBELØP_2017;
        final double forventetRedusert = forventetAvkortet;

        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        Behandling behandling = lagBehandlingATogFLogSN(
            scenario, Arrays.asList(), null, årsinntekterSN, 2014, null);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGSNførAvkorting(periode, forventetBrutto, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }

    @Test
    public void sammeLønnHvertÅrOver6GIkkeVarigEndringReduksjon() {

        //6Gsnitt<PGI<12Gsnitt: Bidrag til beregningsgrunnlaget = 6 + (PGI-6*Gsnitt)/3*Gsnitt
        final double årsinntekt1 = 7.0 * GrunnbeløpForTest.GSNITT_2014;
        final double årsinntekt2 = 7.0 * GrunnbeløpForTest.GSNITT_2015;
        final double årsinntekt3 = 7.0 * GrunnbeløpForTest.GSNITT_2016;
        final List<Double> ÅRSINNTEKT = Arrays.asList(årsinntekt1, årsinntekt2, årsinntekt3);

        //Gjennomsnittlig PGI = SUM(Bidrag til beregningsgrunnlaget)/3 * G
        final double forventetBrutto = 6.333333 * GrunnbeløpForTest.GRUNNBELØP_2017;
        final double forventetAvkortet = 6.0 * GrunnbeløpForTest.GRUNNBELØP_2017;
        final double forventetRedusert = forventetAvkortet;

        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        // Arrange
        Behandling behandling = lagBehandlingATogFLogSN(
            scenario, Arrays.asList(), null, årsinntekterSN, 2014, null);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, Period.ofDays(100), aktiviteter);

        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(behandlingRepository, oversetterTilRegel, new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste));

        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGSNførAvkorting(periode, forventetBrutto, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullføreBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_35);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserBGSNetterAvkorting(periode, forventetBrutto, forventetAvkortet, forventetRedusert, 2016);
    }
}
