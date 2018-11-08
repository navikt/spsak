package no.nav.foreldrepenger.beregningsgrunnlag.verdikjede;

import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.byggFrilansForBehandling;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATetterAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATførAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLetterAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLførAvkorting;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserPeriode;
import static no.nav.foreldrepenger.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserSammenligningsgrunnlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
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
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class FrilanserTest {

    private static final String DUMMY_ORGNR = "999";
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String ARBEIDSFORHOLD_ORGNR1 = "142";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "289";
    private static final String ARBEIDSFORHOLD_ORGNR3 = "308";

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
    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;

    @Inject
    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private FastsettBeregningsgrunnlagPeriodeTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningRepository opptjeningRepository = repositoryProvider.getOpptjeningRepository();



    private OpptjeningInntektArbeidYtelseTjenesteImpl opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);

    private BeregningRepository beregningRepository = repositoryProvider.getBeregningRepository();
    private double seksG;
    private ScenarioMorSøkerForeldrepenger scenario;
    private VirksomhetEntitet beregningVirksomhet1;
    private VirksomhetEntitet beregningVirksomhet2;
    private VirksomhetEntitet beregningVirksomhet3;
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
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(repositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
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

    private static Behandling lagBehandlingATogFL(BehandlingRepositoryProvider repositoryProvider,
            ScenarioMorSøkerForeldrepenger scenario,
            BigDecimal inntektSammenligningsgrunnlag,
            List<BigDecimal> inntektBeregningsgrunnlag,
            BigDecimal inntektFrilans,
            List<VirksomhetEntitet> beregningVirksomhet) {
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
            VerdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, forFrilans, AKTØR_ID, dt, dt.plusMonths(1),
                    inntektSammenligningsgrunnlag, dummyVirksomhet);
            VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, forFrilans, AKTØR_ID, dt, dt.plusMonths(1),
                    inntektFrilans, dummyVirksomhet);
        }

        return scenario.lagre(repositoryProvider);
    }

    private Behandling lagBehandlingFL(ScenarioMorSøkerForeldrepenger scenario,
                                       BigDecimal inntektSammenligningsgrunnlag,
                                       BigDecimal inntektFrilans, VirksomhetEntitet beregningVirksomhet) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        return VerdikjedeTestHjelper.lagBehandlingFL(repositoryProvider, scenario, inntektSammenligningsgrunnlag, inntektFrilans, beregningVirksomhet, fraOgMed, tilOgMed);
    }

    @Test
    public void toArbeidsforholdOgFrilansMedBgOver6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(12 * 28000d, 12 * 14000d);
        final List<Double> refusjonsKrav = Arrays.asList(12 * 20000d, 12 * 15000d);
        final Double sammenligning = 12 * 67500d;
        final Double frilansÅrsinntekt = 12 * 23000d;

        double forventetRedusert1 = Math.min(ÅRSINNTEKT.get(0), refusjonsKrav.get(0));
        double forventetRedusert2 = Math.min(ÅRSINNTEKT.get(1), refusjonsKrav.get(1));

        double forventetBrukersAndel1 = ÅRSINNTEKT.get(0) - forventetRedusert1;
        double forventetBrukersAndel2 = ÅRSINNTEKT.get(1) - forventetRedusert2;
        double forventetBrukersAndelFL = Math.min(frilansÅrsinntekt, seksG - (ÅRSINNTEKT.stream().mapToDouble(Double::doubleValue).sum()));

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(forventetBrukersAndel1, forventetBrukersAndel2);

        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2);
        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());

        // Arrange
        Behandling behandling = lagBehandlingATogFL(repositoryProvider, scenario,
                BigDecimal.valueOf(sammenligning/12),
                månedsinntekter,
                BigDecimal.valueOf(frilansÅrsinntekt/12),
                virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
                månedsinntekter.get(0),BigDecimal.valueOf(refusjonsKrav.get(0)/12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
                månedsinntekter.get(1),BigDecimal.valueOf(refusjonsKrav.get(1)/12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
                VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID)
        );
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);

        // Act 1: kontroller fakta om beregning
        Beregningsgrunnlag beregningsgrunnlag = VerdikjedeTestHjelper.kjørStegOgLagreGrunnlag(behandling, fastsettSkjæringstidspunktOgStatuser, fastsettBeregningsgrunnlagPeriodeTjeneste, beregningsgrunnlagRepository);
        List<AksjonspunktResultat> aksjonspunktResultat = aksjonspunktUtlederForBeregning.utledAksjonspunkterFor(behandling);
        assertThat(aksjonspunktResultat).isEmpty();

        // Assert 1
        assertThat(aksjonspunktResultat).isEmpty();

        // Act 2: foreslå beregningsgrunnlag
        BeregningsgrunnlagRegelResultat resultat = foreslåBeregningsgrunnlagTjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert 2
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 3);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 37L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, ÅRSINNTEKT, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verifiserFLetterAvkorting(periode, frilansÅrsinntekt, forventetBrukersAndelFL, forventetBrukersAndelFL);
    }

    @Test
    public void treArbeidsforholdOgFrilansMedBgUnder6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(12 * 13000d, 12 * 12000d, 12 * 8000d);
        final List<Double> refusjonsKrav = Arrays.asList(12 * 20500d, 12 * 9000d, 12 * 17781d);
        final Double sammenligning = 12 * 60000d;
        final Double frilansÅrsinntekt = 12 * 13000d;

        double forventetRedusert1 = Math.min(ÅRSINNTEKT.get(0), refusjonsKrav.get(0));
        double forventetRedusert2 = Math.min(ÅRSINNTEKT.get(1), refusjonsKrav.get(1));
        double forventetRedusert3 = Math.min(ÅRSINNTEKT.get(2), refusjonsKrav.get(2));

        double forventetBrukersAndel1 = ÅRSINNTEKT.get(0) - forventetRedusert1;
        double forventetBrukersAndel2 = ÅRSINNTEKT.get(1) - forventetRedusert2;
        double forventetBrukersAndel3 = ÅRSINNTEKT.get(2) - forventetRedusert3;
        double forventetBrukersAndelFL = Math.min(frilansÅrsinntekt, seksG - (ÅRSINNTEKT.stream().mapToDouble(Double::doubleValue).sum()));

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(forventetBrukersAndel1, forventetBrukersAndel2, forventetBrukersAndel3);

        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2, beregningVirksomhet3);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());

        // Arrange
        Behandling behandling = lagBehandlingATogFL(repositoryProvider, scenario,
                BigDecimal.valueOf(sammenligning/12),
                månedsinntekter,
                BigDecimal.valueOf(frilansÅrsinntekt/12),
                virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
                månedsinntekter.get(0),BigDecimal.valueOf(refusjonsKrav.get(0)/12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
                månedsinntekter.get(1),BigDecimal.valueOf(refusjonsKrav.get(1)/12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet3,
                månedsinntekter.get(2),BigDecimal.valueOf(refusjonsKrav.get(2)/12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
                VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR3, OpptjeningAktivitetType.ARBEID)
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
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 4);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 233L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, ÅRSINNTEKT, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verifiserFLetterAvkorting(periode, frilansÅrsinntekt, frilansÅrsinntekt, forventetBrukersAndelFL);
    }

    @Test
    public void treArbeidsforholdOgFrilansMedBgOver6gOgRefusjonUnder6G() {

        final List<Double> ÅRSINNTEKT = Arrays.asList(12 * 13000d, 12 * 12000d, 12 * 22500d);
        final List<Double> refusjonsKrav = Arrays.asList(12 * 20500d, 12 * 9000d, 12 * 17781d);
        final Double sammenligning = 12 * 61000d;
        final Double frilansÅrsinntekt = 12 * 14000d;

        double forventetRedusert1 = Math.min(ÅRSINNTEKT.get(0), refusjonsKrav.get(0));
        double forventetRedusert2 = Math.min(ÅRSINNTEKT.get(1), refusjonsKrav.get(1));
        double forventetRedusert3 = Math.min(ÅRSINNTEKT.get(2), refusjonsKrav.get(2));

        double brukersAndel = seksG - forventetRedusert1;

        double forventetBrukersAndel1 = 0;
        double forventetBrukersAndel2 = brukersAndel * ÅRSINNTEKT.get(1) / (ÅRSINNTEKT.get(1) + ÅRSINNTEKT.get(2)) - forventetRedusert2;
        double forventetBrukersAndel3 = brukersAndel * ÅRSINNTEKT.get(2) / (ÅRSINNTEKT.get(1) + ÅRSINNTEKT.get(2)) - forventetRedusert3;
        double forventetBrukersAndelFL = 0;

        final List<Double> avkortetBG = Arrays.asList(forventetBrukersAndel1 + forventetRedusert1, forventetBrukersAndel2 + forventetRedusert2, forventetBrukersAndel3 + forventetRedusert3);

        final List<Double> forventetRedusert = Arrays.asList(forventetRedusert1, forventetRedusert2, forventetRedusert3);
        final List<Double> forventetRedusertBrukersAndel = Arrays.asList(forventetBrukersAndel1, forventetBrukersAndel2, forventetBrukersAndel3);

        List<BigDecimal> månedsinntekter = ÅRSINNTEKT.stream().map((v) -> BigDecimal.valueOf(v / 12)).collect(Collectors.toList());

        List<VirksomhetEntitet> virksomhetene = Arrays.asList(beregningVirksomhet1, beregningVirksomhet2, beregningVirksomhet3);

        // Arrange
        Behandling behandling = lagBehandlingATogFL(repositoryProvider, scenario,
                BigDecimal.valueOf(sammenligning/12),
                månedsinntekter,
                BigDecimal.valueOf(frilansÅrsinntekt/12),
                virksomhetene);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
                månedsinntekter.get(0),BigDecimal.valueOf(refusjonsKrav.get(0)/12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
                månedsinntekter.get(1),BigDecimal.valueOf(refusjonsKrav.get(1)/12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet3,
                månedsinntekter.get(2),BigDecimal.valueOf(refusjonsKrav.get(2)/12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
                VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
                VerdikjedeTestHjelper.leggTilOpptjening(ARBEIDSFORHOLD_ORGNR3, OpptjeningAktivitetType.ARBEID)
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
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 4);
        verifiserBGATførAvkorting(periode, ÅRSINNTEKT, virksomhetene);
        verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 8L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_40);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserBGATetterAvkorting(periode,
            ÅRSINNTEKT, virksomhetene, avkortetBG, forventetRedusert, forventetRedusert, forventetRedusertBrukersAndel, false);
        verifiserFLetterAvkorting(periode, frilansÅrsinntekt, 0.0d, forventetBrukersAndelFL);
    }

    @Test
    public void bareFrilansMedBgUnder6g() {

        final Double sammenligning = 12 * 14000d;
        final Double frilansÅrsinntekt = 12 * 14000d;

        double forventetBrukersAndelFL = frilansÅrsinntekt;

        Behandling behandling = lagBehandlingFL(scenario,
                BigDecimal.valueOf(sammenligning/12),
                BigDecimal.valueOf(frilansÅrsinntekt/12),
                beregningVirksomhet1);

        // Arrange
        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        byggFrilansForBehandling(inntektArbeidYtelseRepository, behandling, DUMMY_ORGNR, aktiviteter);
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
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_38);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_38);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserFLetterAvkorting(periode, frilansÅrsinntekt, frilansÅrsinntekt, forventetBrukersAndelFL);
    }

    @Test
    public void bareFrilansMedBgOver6g() {

        final Double sammenligning = 12 * 70000d;
        final Double frilansÅrsinntekt = 12 * 70000d;

        double forventetBrukersAndelFL = Math.min(seksG, frilansÅrsinntekt);

        Behandling behandling = lagBehandlingFL(scenario,
                BigDecimal.valueOf(sammenligning/12),
                BigDecimal.valueOf(frilansÅrsinntekt/12),
                beregningVirksomhet1);
        // Arrange
        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, aktiviteter, OpptjeningAktivitetType.FRILANS);

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
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_38);

        Beregningsgrunnlag foreslåttBeregningsgrunnlag = resultat.getBeregningsgrunnlag();
        BeregningsgrunnlagPeriode periode = foreslåttBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserFLførAvkorting(periode, frilansÅrsinntekt);
        verifiserSammenligningsgrunnlag(foreslåttBeregningsgrunnlag.getSammenligningsgrunnlag(),
            sammenligning, SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1),
            SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1), 0L);

        // Act 3: fastsette beregningsgrunnlag
        Beregningsgrunnlag fullførtBeregningsgrunnlag = fullføreBeregningsgrunnlagTjeneste.fullføreBeregningsgrunnlag(behandling, foreslåttBeregningsgrunnlag);
        resultat = new BeregningsgrunnlagRegelResultat(fullførtBeregningsgrunnlag, Collections.emptyList());

        // Assert 3
        verifiserBeregningsgrunnlagBasis(resultat, Hjemmel.F_14_7_8_38);

        periode = fullførtBeregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserFLetterAvkorting(periode, frilansÅrsinntekt, seksG, forventetBrukersAndelFL);
    }

}
