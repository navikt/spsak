package no.nav.foreldrepenger.beregningsgrunnlag.verdikjede;

import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATetterAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATførAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGSNetterAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGSNførAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLetterAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLførAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserPeriode;
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

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
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
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KombinasjonArbtakerFrilanserSelvstendigTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final AktørId AKTØR_ID = new AktørId("210195");

    private static final String DUMMY_ORGNR = "999";
    private static final String ARBEIDSFORHOLD_ORGNR1 = "173";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "282";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private BehandlingRepositoryProvider repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(repoRule.getEntityManager()));
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningRepository opptjeningRepository = repositoryProvider.getOpptjeningRepository();
    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;

    @Inject
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;


    private OpptjeningInntektArbeidYtelseTjenesteImpl opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);

    private BeregningRepository beregningRepository = repositoryProvider.getBeregningRepository();
    private long seksG;
    private Long gverdi;
    private VirksomhetEntitet beregningVirksomhet1;
    private VirksomhetEntitet beregningVirksomhet2;
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
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        fastsettSkjæringstidspunktOgStatuser = new FastsettSkjæringstidspunktOgStatuser(oversetterTilRegel,oversetterFraRegel);
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste);
        KontrollerFaktaBeregningTjenesteImpl kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjenesteImpl(repositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        beregningsperiodeTjeneste = new BeregningsperiodeTjeneste(inntektArbeidYtelseTjeneste, beregningsgrunnlagRepository, 5);
        aksjonspunktUtlederForBeregning = new AksjonspunktUtlederForBeregning(repositoryProvider.getAksjonspunktRepository(), faktaOmBeregningTilfelleTjeneste, beregningsperiodeTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel);
        this.fastsettBeregningsgrunnlagPeriodeTjeneste = new FastsettBeregningsgrunnlagPerioderTjenesteImpl(inntektArbeidYtelseTjeneste, beregningInntektsmeldingTjeneste);
        scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        gverdi = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, SKJÆRINGSTIDSPUNKT_OPPTJENING).getVerdi();
        seksG = gverdi * 6;
    }

    public static Behandling lagBehandlingATogFLogSN(BehandlingRepositoryProvider repositoryProvider,
            ScenarioMorSøkerForeldrepenger scenario,
            List<BigDecimal> inntektBeregningsgrunnlag,
            List<VirksomhetEntitet> beregningVirksomhet,
            BigDecimal inntektFrilans,
            List<BigDecimal> årsinntekterSN,
            int førsteÅr,
            BigDecimal årsinntektVarigEndring) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();

        VirksomhetEntitet dummyVirksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(DUMMY_ORGNR)
                .medNavn("Dummyvirksomhet")
                .oppdatertOpplysningerNå()
                .build();
        repositoryProvider.getVirksomhetRepository().lagre(dummyVirksomhet);

        YrkesaktivitetBuilder forFrilans = VerdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, dummyVirksomhet, fraOgMed, tilOgMed, ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
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
            if (inntektFrilans != null) {
                VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, forFrilans, AKTØR_ID, dt, dt.plusMonths(1),
                    inntektFrilans, dummyVirksomhet);
            }
        }

        if (årsinntekterSN != null) {
            for (int ix = 0; ix < 3; ix++) {
                VerdikjedeTestHjelper.lagInntektForSN(inntektArbeidYtelseBuilder, AKTØR_ID, LocalDate.of(førsteÅr+ix, Month.JANUARY, 1), årsinntekterSN.get(ix));
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
        return scenario.lagre(repositoryProvider);
    }

    @Test
    public void toArbeidsforholdOgFrilansMedBgOver6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(12*28000d, 12*14000d);
        final List<Double> refusjonsKrav = Arrays.asList(12*20000d, 12*15000d);
        final Double frilansÅrsinntekt = 12*23000d;

        final double årsinntekt1 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2014, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt2 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2015, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt3 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2016, Month.JANUARY, 1)).getVerdi();
        final List<Double> ÅRSINNTEKT_SN = Arrays.asList(årsinntekt1, årsinntekt2, årsinntekt3);
        final List<Integer> ÅR = Arrays.asList(2014, 2015, 2016);

        final double forventetBruttoSN = BigDecimal.ZERO.max(BigDecimal.valueOf(4.0 * gverdi - (ÅRSINNTEKT.get(0) + ÅRSINNTEKT.get(1)))).doubleValue();

        double forventetRedusert1 = Math.min(ÅRSINNTEKT.get(0), refusjonsKrav.get(0));
        double forventetRedusert2 = Math.min(ÅRSINNTEKT.get(1), refusjonsKrav.get(1));

        double forventetRedusertFLogSN = Math.max(0,  seksG - (ÅRSINNTEKT.stream().mapToDouble(Double::doubleValue).sum()));
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
        Behandling behandling = lagBehandlingATogFLogSN(repositoryProvider, scenario,
                månedsinntekterAT,
                virksomhetene,
                BigDecimal.valueOf(frilansÅrsinntekt/12),
                årsinntekterSN,
                ÅR.get(0), null);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
                månedsinntekterAT.get(0),BigDecimal.valueOf(refusjonsKrav.get(0)/12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
                månedsinntekterAT.get(1),BigDecimal.valueOf(refusjonsKrav.get(1)/12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
                VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
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
        final Double frilansÅrsinntekt = 12*23000d;

        final double årsinntekt1 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2014, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt2 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2015, Month.JANUARY, 1)).getVerdi();
        final double årsinntekt3 = 4.0 * beregningRepository.finnEksaktSats(SatsType.GSNITT, LocalDate.of(2016, Month.JANUARY, 1)).getVerdi();
        final List<Double> ÅRSINNTEKT_SN = Arrays.asList(årsinntekt1, årsinntekt2, årsinntekt3);
        final List<Integer> ÅR = Arrays.asList(2014, 2015, 2016);

        final double forventetBruttoSN = 4*gverdi - frilansÅrsinntekt;

        double forventetBrukersAndelFL = frilansÅrsinntekt;

        final double forventetAvkortetSN = forventetBruttoSN;
        final double forventetRedusertSN = forventetAvkortetSN;

        final List<Double> forventetRedusert = new ArrayList<>();
        final List<Double> forventetRedusertBrukersAndel = new ArrayList<>();

        List<BigDecimal> månedsinntekterAT = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());
        List<BigDecimal> årsinntekterSN = ÅRSINNTEKT_SN.stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        List<VirksomhetEntitet> virksomhetene = new ArrayList<>();

        // Arrange 1
        Behandling behandling = lagBehandlingATogFLogSN(repositoryProvider, scenario,
                månedsinntekterAT,
                virksomhetene,
                BigDecimal.valueOf(frilansÅrsinntekt/12),
                årsinntekterSN,
                ÅR.get(0), null);

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
                VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
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
