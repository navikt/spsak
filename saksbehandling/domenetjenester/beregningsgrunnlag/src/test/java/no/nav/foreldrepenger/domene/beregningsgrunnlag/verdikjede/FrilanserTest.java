package no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.SKJÆRINGSTIDSPUNKT_OPPTJENING;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.byggFrilansForBehandling;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATetterAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBGATførAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserBeregningsgrunnlagBasis;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLetterAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserFLførAvkorting;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.verifiserSammenligningsgrunnlag;
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.SatsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Hjemmel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.AksjonspunktUtlederForBeregning;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningInntektsmeldingTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.BeregningsperiodeTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FaktaOmBeregningTilfelleTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettBeregningsgrunnlagPerioderTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FastsettSkjæringstidspunktOgStatuser;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.ForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.FullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningFrilanserTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.KontrollerFaktaBeregningTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

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

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, resultatRepositoryProvider);
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjenesteImpl inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private AksjonspunktUtlederForBeregning aksjonspunktUtlederForBeregning;
    private ForeslåBeregningsgrunnlag foreslåBeregningsgrunnlagTjeneste;
    private FullføreBeregningsgrunnlag fullføreBeregningsgrunnlagTjeneste;
    private FastsettSkjæringstidspunktOgStatuser fastsettSkjæringstidspunktOgStatuser;

    private FaktaOmBeregningTilfelleTjeneste faktaOmBeregningTilfelleTjeneste;

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private FastsettBeregningsgrunnlagPerioderTjeneste fastsettBeregningsgrunnlagPeriodeTjeneste;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
    private OpptjeningRepository opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();


    private OpptjeningInntektArbeidYtelseTjenesteImpl opptjeningInntektArbeidYtelseTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, resultatRepositoryProvider, periodeTjeneste);

    private SatsRepository beregningRepository = repositoryProvider.getSatsRepository();
    private double seksG;
    private ScenarioMorSøkerForeldrepenger scenario;
    private VirksomhetEntitet beregningVirksomhet1;
    private VirksomhetEntitet beregningVirksomhet2;
    private VirksomhetEntitet beregningVirksomhet3;
    private BeregningsperiodeTjeneste beregningsperiodeTjeneste;
    private BehandlingRepository behandlingRepository;

    private Behandling lagBehandlingATogFL(ScenarioMorSøkerForeldrepenger scenario,
                                           BigDecimal inntektSammenligningsgrunnlag,
                                           List<BigDecimal> inntektBeregningsgrunnlag,
                                           BigDecimal inntektFrilans,
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
        VerdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            inntektSammenligningsgrunnlag, dummyVirksomhet);
        VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            List.of(inntektFrilans), dummyVirksomhet);

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
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjeneste(resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        fastsettSkjæringstidspunktOgStatuser = new FastsettSkjæringstidspunktOgStatuser(oversetterTilRegel, oversetterFraRegel);
        BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste = new BeregningInntektsmeldingTjeneste(repositoryProvider, inntektArbeidYtelseTjeneste);
        KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste = new KontrollerFaktaBeregningTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste, hentGrunnlagsdataTjeneste, beregningInntektsmeldingTjeneste);
        KontrollerFaktaBeregningFrilanserTjeneste faktaBeregningFrilanserTjeneste = new KontrollerFaktaBeregningFrilanserTjeneste(resultatRepositoryProvider, inntektArbeidYtelseTjeneste);
        faktaOmBeregningTilfelleTjeneste = new FaktaOmBeregningTilfelleTjeneste(resultatRepositoryProvider, kontrollerFaktaBeregningTjeneste, faktaBeregningFrilanserTjeneste);
        beregningsperiodeTjeneste = new BeregningsperiodeTjeneste(inntektArbeidYtelseTjeneste, beregningsgrunnlagRepository, 5);
        aksjonspunktUtlederForBeregning = new AksjonspunktUtlederForBeregning(repositoryProvider.getAksjonspunktRepository(), faktaOmBeregningTilfelleTjeneste, beregningsperiodeTjeneste);
        foreslåBeregningsgrunnlagTjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fullføreBeregningsgrunnlagTjeneste = new FullføreBeregningsgrunnlag(behandlingRepository, oversetterTilRegel, oversetterFraRegel);
        this.fastsettBeregningsgrunnlagPeriodeTjeneste = new FastsettBeregningsgrunnlagPerioderTjeneste(inntektArbeidYtelseTjeneste, beregningInntektsmeldingTjeneste);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        seksG = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, SKJÆRINGSTIDSPUNKT_OPPTJENING).getVerdi() * 6;
    }

    private Behandling lagBehandlingFL(ScenarioMorSøkerForeldrepenger scenario,
                                       BigDecimal inntektSammenligningsgrunnlag,
                                       BigDecimal inntektFrilans, VirksomhetEntitet beregningVirksomhet) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        return VerdikjedeTestHjelper.lagBehandlingFL(repositoryProvider, resultatRepositoryProvider, scenario, inntektSammenligningsgrunnlag, inntektFrilans, beregningVirksomhet, fraOgMed, tilOgMed);
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
        Behandling behandling = lagBehandlingATogFL(scenario,
            BigDecimal.valueOf(sammenligning / 12),
            månedsinntekter,
            BigDecimal.valueOf(frilansÅrsinntekt / 12),
            beregningVirksomhet1, beregningVirksomhet2);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID)
        );
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        opptjeningRepository.lagreOpptjeningsperiode(behandlingsresultat, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandlingsresultat, Period.ofDays(100), aktiviteter);

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
        Behandling behandling = lagBehandlingATogFL(scenario,
            BigDecimal.valueOf(sammenligning / 12),
            månedsinntekter,
            BigDecimal.valueOf(frilansÅrsinntekt / 12),
            beregningVirksomhet1, beregningVirksomhet2, beregningVirksomhet3);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet3,
            månedsinntekter.get(2), BigDecimal.valueOf(refusjonsKrav.get(2) / 12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR3, OpptjeningAktivitetType.ARBEID)
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
        Behandling behandling = lagBehandlingATogFL(scenario,
            BigDecimal.valueOf(sammenligning / 12),
            månedsinntekter,
            BigDecimal.valueOf(frilansÅrsinntekt / 12),
            beregningVirksomhet1, beregningVirksomhet2, beregningVirksomhet3);

        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet1,
            månedsinntekter.get(0), BigDecimal.valueOf(refusjonsKrav.get(0) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet2,
            månedsinntekter.get(1), BigDecimal.valueOf(refusjonsKrav.get(1) / 12));
        VerdikjedeTestHjelper.opprettInntektsmeldingMedRefusjonskrav(repositoryProvider, behandling, beregningVirksomhet3,
            månedsinntekter.get(2), BigDecimal.valueOf(refusjonsKrav.get(2) / 12));

        List<OpptjeningAktivitet> aktiviteter = Arrays.asList(
            VerdikjedeTestHjelper.opprettAktivitetFor(DUMMY_ORGNR, OpptjeningAktivitetType.FRILANS),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR1, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR2, OpptjeningAktivitetType.ARBEID),
            VerdikjedeTestHjelper.opprettAktivitetFor(ARBEIDSFORHOLD_ORGNR3, OpptjeningAktivitetType.ARBEID)
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
            BigDecimal.valueOf(sammenligning / 12),
            BigDecimal.valueOf(frilansÅrsinntekt / 12),
            beregningVirksomhet1);

        // Arrange
        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        byggFrilansForBehandling(inntektArbeidYtelseRepository, behandling, DUMMY_ORGNR, aktiviteter);
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
            BigDecimal.valueOf(sammenligning / 12),
            BigDecimal.valueOf(frilansÅrsinntekt / 12),
            beregningVirksomhet1);
        // Arrange
        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        VerdikjedeTestHjelper.leggTilOpptjening(DUMMY_ORGNR, aktiviteter, OpptjeningAktivitetType.FRILANS);

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
