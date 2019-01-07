package no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATetterAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATførAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGSNetterAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGSNførAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLetterAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLførAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserPeriode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.SatsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
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
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class KombinasjonArbtakerFrilanserSelvstendigTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final AktørId AKTØR_ID = new AktørId("210195");

    private static final String DUMMY_ORGNR = "999";
    private static final String ARBEIDSFORHOLD_ORGNR1 = "173";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "282";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningRepository opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;

    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private OpptjeningInntektArbeidYtelseTjenesteImpl opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, periodeTjeneste);

    private SatsRepository beregningRepository = repositoryProvider.getSatsRepository();
    private long seksG;
    private Long gverdi;
    private VirksomhetEntitet beregningVirksomhet1;
    private VirksomhetEntitet beregningVirksomhet2;
    private ScenarioMorSøkerForeldrepenger scenario;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;

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
            EgenNæringBuilder egenNæringBuilder = EgenNæringBuilder.ny()
                .medBruttoInntekt(årsinntektVarigEndring)
                .medVarigEndring(true)
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(1), SKJÆRINGSTIDSPUNKT_BEREGNING))
                .medEndringDato(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(1));
            scenario.medOppgittOpptjening(OppgittOpptjeningBuilder.ny()
                .leggTilEgneNæringer(Arrays.asList(egenNæringBuilder)));
        }
        return scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Before
    public void setup() {
        if (beregningVirksomhet1 != null) {
            return;
        }
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
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        fastsettSkjæringstidspunktOgStatuser = new FastsettSkjæringstidspunktOgStatuser(oversetterTilRegel, oversetterFraRegel);
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
        KontrollerFaktaBeregningTjenesteImpl kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjenesteImpl(resultatRepositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        beregningsperiodeTjeneste = new BeregningsperiodeTjeneste(inntektArbeidYtelseTjeneste, beregningsgrunnlagRepository, 5);
        KontrollerFaktaBeregningFrilanserTjeneste kontrollerFaktaBeregningFrilaserTjeneste = new KontrollerFaktaBeregningFrilanserTjenesteImpl(resultatRepositoryProvider, inntektArbeidYtelseTjeneste);
        faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(resultatRepositoryProvider, kontrollerFaktaBeregningTjeneste, kontrollerFaktaBeregningFrilaserTjeneste);
        aksjonspunktUtlederForBeregning = new AksjonspunktUtlederForBeregning(repositoryProvider.getAksjonspunktRepository(), faktaOmBeregningTilfelleTjeneste, beregningsperiodeTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel);
        this.fastsettBeregningsgrunnlagPeriodeTjeneste = new FastsettBeregningsgrunnlagPerioderTjenesteImpl(inntektArbeidYtelseTjeneste, beregningInntektsmeldingTjeneste);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        gverdi = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, SKJÆRINGSTIDSPUNKT_OPPTJENING).getVerdi();
        seksG = gverdi * 6;
    }

    @Test
    public void toArbeidsforholdOgFrilansMedBgOver6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(12 * 28000d, 12 * 14000d);
        final List<Double> refusjonsKrav = Arrays.asList(12 * 20000d, 12 * 15000d);
        final Double frilansÅrsinntekt = 12 * 23000d;

        final double årsinntekt1 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2014, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt2 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2015, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt3 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2016, Month.JANUARY, 1)).getVerdi();
        final List<Double> ÅRSINNTEKT_SN = Arrays.asList(årsinntekt1, årsinntekt2, årsinntekt3);
        final List<Integer> ÅR = Arrays.asList(2014, 2015, 2016);

        final double forventetBruttoSN = BigDecimal.ZERO.max(BigDecimal.valueOf(4.0 * gverdi - (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1)))).doubleValue();

        double forventetRedusert1 = Math.min(ÅRSINNTEKT.get(0), refusjonsKrav.get(0));
        double forventetRedusert2 = Math.min(ÅRSINNTEKT.get(1), refusjonsKrav.get(1));

        double forventetRedusertFLogSN = Math.max(0, seksG - (ÅRSINNTEKT.stream().mapToDouble(Double::doubleValue).sum()));
        double forventetBrukersAndelFL = forventetRedusertFLogSN * frilansÅrsinntekt / (frilansÅrsinntekt + forventetBruttoSN);

        final double forventetAvkortetSN = forventetRedusertFLogSN * forventetBruttoSN / (frilansÅrsinntekt + forventetBruttoSN);
        final double forventetRedusertSN = forventetAvkortetSN;

        double forventetBrukersAndel1 = ÅRSINNTEKT.get(0) - forventetRedusert1;
        double forventetBrukersAndel2 = ÅRSINNTEKT.get(1) - forventetRedusert2;

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(forventetBrukersAndel1, forventetBrukersAndel2);

        List<BigDecimal> månedsinntekterAT = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT_SN.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2);

        // Arrange
        Behandling behandling = lagBehandlingATogFLogSN(scenario,
            månedsinntekterAT,
            BigDecimal.valueOf(frilansÅrsinntekt / 12), årsinntekterSN, ÅR.get(0), null,
            beregningVirksomhet1, beregningVirksomhet2);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekterAT.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekterAT.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling.getBehandlingsresultat(), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling.getBehandlingsresultat(), Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_43);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 4);
        verifiserBGSNførAvkorting(periode, forventetBruttoSN, 2016);
        assertThat(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserFLførAvkorting(periode, frilansÅrsinntekt);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullføreBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_43);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserBGSNetterAvkorting(periode, forventetBruttoSN, forventetAvkortetSN, forventetRedusertSN, 2016);

        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, ÅRSINNTEKT, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verifiserFLetterAvkorting(periode, frilansÅrsinntekt, forventetBrukersAndelFL, forventetBrukersAndelFL);
    }

    @Test
    public void frilansOgSNMedBgFraArbeidsforholdUnder6G() {

        final List<Double> ÅRSINNTEKT = new ArrayList<>();
        final Double frilansÅrsinntekt = 12 * 23000d;

        final double årsinntekt1 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2014, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt2 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2015, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt3 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2016, Month.JANUARY, 1)).getVerdi();
        final List<Double> ÅRSINNTEKT_SN = Arrays.asList(årsinntekt1, årsinntekt2, årsinntekt3);
        final List<Integer> ÅR = Arrays.asList(2014, 2015, 2016);

        final double forventetBruttoSN = 4 * gverdi - frilansÅrsinntekt;

        double forventetBrukersAndelFL = frilansÅrsinntekt;

        final double forventetAvkortetSN = forventetBruttoSN;
        final double forventetRedusertSN = forventetAvkortetSN;

        final List<Double> forventetRedusert = new ArrayList<>();
        final List<Double> forventetRedusertBrukersAndel = new ArrayList<>();

        List<BigDecimal> månedsinntekterAT = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT_SN.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        List<VirksomhetEntitet> virksomhetene = new ArrayList<>();

        // Arrange 1
        Behandling behandling = lagBehandlingATogFLogSN(scenario,
            månedsinntekterAT,
            BigDecimal.valueOf(frilansÅrsinntekt / 12), årsinntekterSN, ÅR.get(0), null);

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.NÆRING)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling.getBehandlingsresultat(), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling.getBehandlingsresultat(), Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_42);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 2);
        verifiserBGSNførAvkorting(periode, forventetBruttoSN, 2016);
        assertThat(beregningsgrunnlag.getSammenligningsgrunnlag()).isNull();

        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserFLførAvkorting(periode, frilansÅrsinntekt);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullføreBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullføreBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_42);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserBGSNetterAvkorting(periode, forventetBruttoSN, forventetAvkortetSN, forventetRedusertSN, 2016);

        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, ÅRSINNTEKT, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verifiserFLetterAvkorting(periode, frilansÅrsinntekt, forventetBrukersAndelFL, forventetBrukersAndelFL);
    }
}
