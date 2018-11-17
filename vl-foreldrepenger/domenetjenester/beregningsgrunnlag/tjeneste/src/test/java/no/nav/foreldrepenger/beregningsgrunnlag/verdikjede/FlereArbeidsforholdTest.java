package no.nav.foreldrepenger.beregningsgrunnlag.verdikjede;

import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATetterAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATetterOverstyring;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATførAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagMedAksjonspunkt;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserPeriode;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserSammenligningsgrunnlag;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningInntektsmeldingTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningInntektsmeldingTjenesteImpl;
import no.nav.foreldrepenger.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettBeregningsgrunnlagPeriodeTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettBeregningsgrunnlagPerioderTjenesteImpl;
import no.nav.foreldrepenger.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.FullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.beregningsgrunnlag.KontrollerFaktaBeregningTjenesteImpl;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class FlereArbeidsforholdTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String ARBEIDSFORHOLD_ORGNR1 = "132";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "259";
    private static final String ARBEIDSFORHOLD_ORGNR3 = "368";
    private static final String ARBEIDSFORHOLD_ORGNR4 = "403";
    private static double seksG;
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private BehandlingRepositoryProvider repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(repoRule.getEntityManager()));

    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;

    @Inject
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    @Mock
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, arbeidsforholdTjeneste, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private OpptjeningRepository opptjeningRepository = repositoryProvider.getOpptjeningRepository();
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private OpptjeningInntektArbeidYtelseTjenesteImpl opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);

    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    private BeregningRepository beregningRepository = repositoryProvider.getBeregningRepository();

    private VirksomhetEntitet beregningVirksomhet1;
    private VirksomhetEntitet beregningVirksomhet2;
    private VirksomhetEntitet beregningVirksomhet3;
    private VirksomhetEntitet beregningVirksomhet4;
    private ScenarioMorSøkerForeldrepenger scenario;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;

    @Before
    public void setup() {
        beregningVirksomhet1 = new VirksomhetEntitet.Builder()
            .medOrgnr(ARBEIDSFORHOLD_ORGNR1)
            .medNavn("BeregningVirksomhet nr 1")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(beregningVirksomhet1);
        beregningVirksomhet2 = new VirksomhetEntitet.Builder()
            .medOrgnr(ARBEIDSFORHOLD_ORGNR2)
            .medNavn("BeregningVirksomhet nr 2")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(beregningVirksomhet2);
        beregningVirksomhet3 = new VirksomhetEntitet.Builder()
            .medOrgnr(ARBEIDSFORHOLD_ORGNR3)
            .medNavn("BeregningVirksomhet nr 3")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(beregningVirksomhet3);
        beregningVirksomhet4 = new VirksomhetEntitet.Builder()
            .medOrgnr(ARBEIDSFORHOLD_ORGNR4)
            .medNavn("BeregningVirksomhet nr 4")
            .oppdatertOpplysningerNå()
            .build();
        repositoryProvider.getVirksomhetRepository().lagre(beregningVirksomhet4);
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
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
        this.fastsettBeregningsgrunnlagPeriodeTjeneste = new FastsettBeregningsgrunnlagPerioderTjenesteImpl(inntektArbeidYtelseTjeneste, beregningInntektsmeldingTjeneste);
        scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        seksG = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, SKJÆRINGSTIDSPUNKT_OPPTJENING).getVerdi() * 6;
    }

    private Behandling lagBehandlingAT(ScenarioMorSøkerForeldrepenger scenario,
                                       BigDecimal inntektSammenligningsgrunnlag,
                                       List<BigDecimal> inntektBeregningsgrunnlag,
                                       List<VirksomhetEntitet> beregningVirksomhet) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();

        List<YrkesaktivitetBuilder> forArbeidsforhold =
            beregningVirksomhet.stream()
                .map(v -> VerdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, v, fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD))
                .collect(Collectors.toList());

        for (LocalDate dt = fraOgMed; dt.isBefore(tilOgMed); dt = dt.plusMonths(1)) {
            for (int i = 0; i < forArbeidsforhold.size(); i++) {
                VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder,
                    forArbeidsforhold.get(i),
                    AKTØR_ID, dt, dt.plusMonths(1),
                    inntektBeregningsgrunnlag.get(i),
                    beregningVirksomhet.get(i));
            }
            VerdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, forArbeidsforhold.get(0), AKTØR_ID, dt, dt.plusMonths(1),
                inntektSammenligningsgrunnlag, beregningVirksomhet.get(0));
        }

        return scenario.lagre(repositoryProvider);
    }

    @Test
    public void ettArbeidsforholdMedAvrundetDagsats() {

        final double DAGSATS = 1959.76;
        final List<Double> ÅRSINNTEKT = Arrays.asList(DAGSATS * 260);
        final Double bg = ÅRSINNTEKT.get(0);

        final double forventetAvkortet = ÅRSINNTEKT.get(0);
        final double forventetRedusert = forventetAvkortet;

        final long forventetDagsats = 1960;
        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1);

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), månedsinntekter.get(0));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 1
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1, forventetDagsats);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, Arrays.asList(forventetRedusert), ÅRSINNTEKT, Arrays.asList(forventetRedusert), Arrays.asList(0.0d), false);
    }

    @Test
    public void ettArbeidsforholdMedOverstyringUnder6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(180000d);
        final Double bg = ÅRSINNTEKT.get(0);
        final Double overstyrt = 200000d;

        final double forventetAvkortet1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert1 = forventetAvkortet1;

        final long forventetDagsats = Math.round(overstyrt / 260);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1);

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12 / 2),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), månedsinntekter.get(0));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagMedAksjonspunkt(resultat);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg / 2, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 1000L);

        // Arrange 2: Overstyring
        periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1, forventetDagsats);
        verifiserBGATetterOverstyring(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            bg, beregningVirksomhet1, overstyrt, overstyrt, overstyrt, bg, forventetAvkortet1, overstyrt - forventetAvkortet1, forventetRedusert1, overstyrt - forventetRedusert1);
    }

    @Test
    public void ettArbeidsforholdMedOverstyringOver6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(480000d);
        final Double bg = ÅRSINNTEKT.get(0);
        final Double overstyrt = 700000d;

        final double forventetAvkortet1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert1 = forventetAvkortet1;

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1);

        final long forventetDagsats = Math.round(seksG / 260);

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12 / 2),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), månedsinntekter.get(0));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagMedAksjonspunkt(resultat);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg / 2, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 1000L);

        // Arrange 2: Overstyring
        periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1, forventetDagsats);
        verifiserBGATetterOverstyring(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            bg, beregningVirksomhet1, overstyrt, seksG, seksG, bg, forventetAvkortet1, seksG - forventetAvkortet1, forventetRedusert1, seksG - forventetRedusert1);
    }

    @Test
    public void ettArbeidsforholdMedOverstyringOver6GOgReduksjon() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(480000d);
        final Double bg = ÅRSINNTEKT.get(0);
        final Double overstyrt = 700000d;

        final double forventetAvkortet = seksG;
        final double forventetRedusert = forventetAvkortet;
        final double forventetAvkortet1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert1 = forventetAvkortet1;

        final long forventetDagsats = Math.round(forventetRedusert / 260);
        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1);

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(ÅRSINNTEKT.get(0) / 12 / 2),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), månedsinntekter.get(0));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        //Nytt setup for 80% dekningsgrad
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(
            repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(oversetterTilRegel, new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste));

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagMedAksjonspunkt(resultat);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            bg / 2, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 1000L);

        // Arrange 2: Overstyring
        periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1, forventetDagsats);
        verifiserBGATetterOverstyring(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0),
            bg, beregningVirksomhet1, overstyrt, forventetAvkortet, forventetRedusert, bg,
            forventetAvkortet1, seksG - forventetAvkortet1,
            forventetRedusert1, seksG - forventetRedusert1);
    }

    @Test
    public void toArbeidsforholdMedBgUnder6gOgFullRefusjon() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(180000d, 72000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        final double forventetRedusert1 = ÅRSINNTEKT.get(0);
        final double forventetRedusert2 = ÅRSINNTEKT.get(1);

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(0d, 0d);

        final long forventetDagsats = Math.round(forventetRedusert1 / 260) + Math.round(forventetRedusert2 / 260);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2);

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), månedsinntekter.get(0));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), månedsinntekter.get(1));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 2);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 2, forventetDagsats);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, forventetRedusert, ÅRSINNTEKT, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void toArbeidsforholdMedBgOver6gOgFullRefusjon() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(448000d, 336000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        final double forventetRedusert1 = seksG * ÅRSINNTEKT.get(0) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));
        final double forventetRedusert2 = seksG * ÅRSINNTEKT.get(1) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(0d, 0d);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2);

        final long forventetDagsats = forventetRedusert.stream().mapToLong(dv -> Math.round(dv / 260)).sum() +
            forventetRedusertBrukersAndel.stream().mapToLong(dv -> Math.round(dv / 260)).sum();

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), månedsinntekter.get(0));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), månedsinntekter.get(1));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 2);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 2, forventetDagsats);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, forventetRedusert, ÅRSINNTEKT, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void fireArbeidsforholdMedBgOver6gOgDelvisRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(400000d, 500000d, 300000d, 100000d);
        final List<Double> refusjonsKrav = Arrays.asList(200000d, 150000d, 300000d, 100000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        double fordelingRunde2 = seksG - (refusjonsKrav.get(0) + refusjonsKrav.get(1));
        double forventetRedusert1 = refusjonsKrav.get(0);
        double forventetRedusert2 = refusjonsKrav.get(1);
        double forventetRedusert3 = fordelingRunde2 * ÅRSINNTEKT.get(2) / (ÅRSINNTEKT.get(2) + ÅRSINNTEKT.get(3));
        double forventetRedusert4 = fordelingRunde2 * ÅRSINNTEKT.get(3) / (ÅRSINNTEKT.get(2) + ÅRSINNTEKT.get(3));

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3, forventetRedusert4);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(0d, 0d, 0d, 0d);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2
            , beregningVirksomhet3, beregningVirksomhet4);

        final long forventetDagsats = forventetRedusert.stream().mapToLong(dv -> Math.round(dv / 260)).sum() +
            forventetRedusertBrukersAndel.stream().mapToLong(dv -> Math.round(dv / 260)).sum();

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet3,
            månedsinntekter.get(2), BigDecimal.valueOf(refusjonsKrav.get(2) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet4,
            månedsinntekter.get(3), BigDecimal.valueOf(refusjonsKrav.get(3) / 12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR3, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR4, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 4);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 4, forventetDagsats);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, forventetRedusert, refusjonsKrav, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void fireArbeidsforholdMedBgOver6gOgDelvisRefusjonOver6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(400000d, 500000d, 300000d, 100000d);
        final List<Double> refusjonsKrav = Arrays.asList(200000d, 150000d, 100000d, 42000d);

        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);

        double arb1 = refusjonsKrav.get(0);
        double arb2 = refusjonsKrav.get(1);
        double arb3 = refusjonsKrav.get(2);
        double arb4 = refusjonsKrav.get(3);

        double rest = seksG - (arb1 + arb4);
        double bruker1 = 0.0d;
        double bruker2 = rest * ÅRSINNTEKT.get(1) / (ÅRSINNTEKT.get(1) + ÅRSINNTEKT.get(2)) - arb2;
        double bruker3 = rest * ÅRSINNTEKT.get(2) / (ÅRSINNTEKT.get(1) + ÅRSINNTEKT.get(2)) - arb3;
        double bruker4 = 0.0d;

        final List<Double> forventetRedusert = Arrays.asList(arb1, arb2, arb3, arb4);

        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(bruker1, bruker2, bruker3, bruker4);

        final long forventetDagsats = forventetRedusert.stream().mapToLong(dv -> Math.round(dv / 260)).sum() +
            forventetRedusertBrukersAndel.stream().mapToLong(dv -> Math.round(dv / 260)).sum();

        final List<Double> forventetAvkortet = Arrays.asList(arb1 + bruker1, arb2 + bruker2, arb3 + bruker3, arb4 + bruker4);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2
            , beregningVirksomhet3, beregningVirksomhet4);

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet3,
            månedsinntekter.get(2), BigDecimal.valueOf(refusjonsKrav.get(2) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet4,
            månedsinntekter.get(3), BigDecimal.valueOf(refusjonsKrav.get(3) / 12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR3, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR4, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 4);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 4, forventetDagsats);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, forventetAvkortet, refusjonsKrav, forventetRedusert, forventetRedusertBrukersAndel, false);
    }

    @Test
    public void toArbeidsforholdMedOverstyringEtterTilbakeføringOver6GMedRefusjonOver6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(720000d, 720000d);
        final Double totalÅrsinntekt = ÅRSINNTEKT.stream().reduce((v1, v2) -> v1 + v2).orElse(null);
        final List<Double> refusjonsKrav = Arrays.asList(seksG, seksG);

        final double forventetRedusert1 = seksG * ÅRSINNTEKT.get(0) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));
        final double forventetRedusert2 = seksG * ÅRSINNTEKT.get(1) / (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1));

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(0d, 0d);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2);

        // Arrange 1
        Behandling behandling = lagBehandlingAT(scenario,
            BigDecimal.valueOf(totalÅrsinntekt / 12),
            månedsinntekter,
            virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta for beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        Beregningsgrunnlag beregningsgrunnlagEtter1 = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periodeEtter1 = beregningsgrunnlagEtter1.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periodeEtter1, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 2);
        verifiserBGATførAvkorting(periodeEtter1, ÅRSINNTEKT, virksomhetene);
        verifiserSammenligningsgrunnlag(beregningsgrunnlagEtter1.getSammenligningsgrunnlag(),
            totalÅrsinntekt, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Arrange 2: Overstyring
        double overstyrt = 700000.0;
        final BeregningsgrunnlagPeriode periode1 = periodeEtter1;
        periodeEtter1.getBeregningsgrunnlagPrStatusOgAndelList().forEach(af -> BeregningsgrunnlagPrStatusOgAndel.builder(af)
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt))
            .build(periode1));
        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, beregningsgrunnlagEtter1);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Arrange 3: Tilbakehopp med Overstyring
        double overstyrt2 = 720000.0;
        Beregningsgrunnlag beregningsgrunnlagEtter2 = resultat.getBeregningsgrunnlag();
        final BeregningsgrunnlagPeriode periodeEtter2 = beregningsgrunnlagEtter2.getBeregningsgrunnlagPerioder().get(0);
        periodeEtter2.getBeregningsgrunnlagPrStatusOgAndelList().forEach(af -> BeregningsgrunnlagPrStatusOgAndel.builder(af)
            .medOverstyrtPrÅr(BigDecimal.valueOf(overstyrt2))
            .build(periodeEtter2));
        // Act 4: fastsette beregningsgrunnlag
        Beregningsgrunnlag beregningsgrunnlagEtter3 = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, beregningsgrunnlagEtter2);
        resultat = new BeregningsgrunnlagRegelResultat(beregningsgrunnlagEtter3, Collections.emptyList());

        // Assert 3-4
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_30);

        BeregningsgrunnlagPeriode periodeEtter3 = beregningsgrunnlagEtter3.getBeregningsgrunnlagPerioder().get(0);
        Long forvetetAndelSum = Math.round((seksG / 2) / 260) * 2;
        verifiserPeriode(periodeEtter3, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 2, forvetetAndelSum);
        verifiserBGATetterAvkorting(periodeEtter3,
            ÅRSINNTEKT, virksomhetene, forventetRedusert, refusjonsKrav, forventetRedusert, forventetRedusertBrukersAndel, true);
    }
}
