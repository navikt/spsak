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
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPeriodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPerioderTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.GrunnbeløpForTest;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class SelvstendigNæringsdrivendeTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String DUMMY_ORGNR = "999";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private BehandlingRepositoryProvider repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(repoRule.getEntityManager()));

    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;

    @Inject
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningRepository opptjeningRepository = repositoryProvider.getOpptjeningRepository();
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;

    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private OpptjeningInntektArbeidYtelseTjenesteImpl opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);

    private ScenarioMorSøkerForeldrepenger scenario;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;

    @Before
    public void setup() {
        OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        fastsettSkjæringstidspunktOgStatuser = new FastsettSkjæringstidspunktOgStatuser(oversetterTilRegel, oversetterFraRegel);
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
        KontrollerFaktaBeregningTjenesteImpl kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        beregningsperiodeTjeneste = new BeregningsperiodeTjeneste(inntektArbeidYtelseTjeneste, beregningsgrunnlagRepository, 5);
        aksjonspunktUtlederForBeregning = new AksjonspunktUtlederForBeregning(repositoryProvider.getAksjonspunktRepository(), faktaOmBeregningTilfelleTjeneste, beregningsperiodeTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        this.fastsettBeregningsgrunnlagPeriodeTjeneste = new FastsettBeregningsgrunnlagPerioderTjenesteImpl(inntektArbeidYtelseTjeneste, beregningInntektsmeldingTjeneste);
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
        Behandling behandling = KombinasjonArbtakerFrilanserSelvstendigTest.lagBehandlingATogFLogSN(repositoryProvider,
            scenario, Arrays.asList(), Arrays.asList(), null, årsinntekterSN, 2014, BigDecimal.valueOf(12 * varigEndringMånedsinntekt));

//        VerdikjedeTestHjelper.settoppÅrsinntekterVarigEndring(beregningsgrunnlagMockRepository, ÅRSINNTEKT, ÅR, varigEndringMånedsinntekt);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

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
        Behandling behandling = KombinasjonArbtakerFrilanserSelvstendigTest.lagBehandlingATogFLogSN(repositoryProvider,
            scenario, Arrays.asList(), Arrays.asList(), null, årsinntekterSN, 2014, null);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

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
        Behandling behandling = KombinasjonArbtakerFrilanserSelvstendigTest.lagBehandlingATogFLogSN(repositoryProvider,
            scenario, Arrays.asList(), Arrays.asList(), null, årsinntekterSN, 2014, null);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

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
        Behandling behandling = KombinasjonArbtakerFrilanserSelvstendigTest.lagBehandlingATogFLogSN(repositoryProvider,
            scenario, Arrays.asList(), Arrays.asList(), null, årsinntekterSN, 2014, null);
        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(oversetterTilRegel, new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste));

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
