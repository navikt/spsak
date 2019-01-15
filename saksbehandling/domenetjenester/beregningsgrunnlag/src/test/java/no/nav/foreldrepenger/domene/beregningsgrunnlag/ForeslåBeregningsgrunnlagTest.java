package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper.lagBehandlingFor_AT_SN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.Kopimaskin;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Sammenligningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.IAYRegisterInnhentingFPTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.regelmodelltilvl.MapBeregningsgrunnlagFraRegelTilVL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell.MapBeregningsgrunnlagFraVLTilRegel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.BeregningsgrunnlagRegelResultat;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class ForeslåBeregningsgrunnlagTest {

    private static final double MÅNEDSINNTEKT1 = 12345d;
    private static final double MÅNEDSINNTEKT2 = 6000d;
    private static final double ÅRSINNTEKT1 = MÅNEDSINNTEKT1 * 12;
    private static final double ÅRSINNTEKT2 = MÅNEDSINNTEKT2 * 12;
    private static final double NATURALYTELSE_I_PERIODE_2 = 200d;
    private static final double NATURALYTELSE_I_PERIODE_3 = 400d;
    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.APRIL, 10);
    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String ARBEIDSFORHOLD_ORGNR1 = "654";
    private static final String ARBEIDSFORHOLD_ORGNR2 = "765";
    private static final LocalDate MINUS_YEARS_1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_FOM = LocalDate.now().minusYears(1);
    private static final LocalDate ARBEIDSPERIODE_TOM = LocalDate.now().plusYears(2);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private final ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private ForeslåBeregningsgrunnlag tjeneste;

    @Mock
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    @Mock
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;

    private Behandling behandling;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private Map<BeregningsgrunnlagPrStatusOgAndel, Yrkesaktivitet> yrkesaktivitetMap = new HashMap<>();
    private VirksomhetEntitet beregningVirksomhet1;
    private VirksomhetEntitet beregningVirksomhet2;
    private YrkesaktivitetBuilder yrkesaktivitetBuilder;
    private ScenarioMorSøkerForeldrepenger scenario;

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
        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjeneste(resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        tjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        lagBeregningsgrunnlagAT(scenario);
        when(kontrollerFaktaBeregningTjeneste.hentAndelerForKortvarigeArbeidsforhold(any())).thenAnswer((b) -> yrkesaktivitetMap);
        beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagAT(ScenarioMorSøkerForeldrepenger scenario) {
        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, beregningVirksomhet1))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
            ));
        return beregningsgrunnlagBuilder.build();
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagATFL_SN(ScenarioMorSøkerForeldrepenger scenario, boolean nyIArbeidslivet) {
        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.KOMBINERT_AT_SN));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, beregningVirksomhet1))
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
            )
            .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medNyIArbeidslivet(nyIArbeidslivet)
                .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder()
                    .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                    .medArbeidsperiodeTom(LocalDate.now().plusYears(2))))
        );
        return beregningsgrunnlagBuilder.build();
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagFL(ScenarioMorSøkerForeldrepenger scenario) {
        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.FRILANSER));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, beregningVirksomhet1))
                .medAktivitetStatus(AktivitetStatus.FRILANSER)
                .medInntektskategori(Inntektskategori.FRILANSER)
                .medBeregningsperiode(SKJÆRINGSTIDSPUNKT_BEREGNING.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1))
            ));
        return beregningsgrunnlagBuilder.build();
    }

    private BGAndelArbeidsforhold.Builder lagBgAndelArbeidsforhold(LocalDate fom, LocalDate tom, VirksomhetEntitet virksomhet) {
        return BGAndelArbeidsforhold.builder().medArbeidsperiodeFom(fom).medArbeidsperiodeTom(tom).medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
    }

    private Behandling lagBehandling(ScenarioMorSøkerForeldrepenger scenario,
                                     BigDecimal inntektSammenligningsgrunnlag,
                                     BigDecimal inntektBeregningsgrunnlag, VirksomhetEntitet... virksomheter) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);

        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseBuilder = scenario.getInntektArbeidYtelseScenarioTestBuilder().getKladd();
        VerdikjedeTestHjelper.lagAktørArbeid(inntektArbeidYtelseBuilder, AKTØR_ID, fraOgMed, tilOgMed, ArbeidType.ORDINÆRT_ARBEIDSFORHOLD, virksomheter);
        List<DatoIntervallEntitet> perioder = VerdikjedeTestHjelper.utledPerioderMellomFomTom(fraOgMed, tilOgMed);
        VerdikjedeTestHjelper.lagInntektForSammenligning(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            inntektSammenligningsgrunnlag, virksomheter);
        VerdikjedeTestHjelper.lagInntektForArbeidsforhold(inntektArbeidYtelseBuilder, AKTØR_ID, perioder,
            List.of(inntektBeregningsgrunnlag), virksomheter);

        return scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    private Behandling lagBehandlingFL(ScenarioMorSøkerForeldrepenger scenario,
                                       BigDecimal inntektSammenligningsgrunnlag,
                                       BigDecimal inntektFrilans, VirksomhetEntitet beregningVirksomhet) {
        LocalDate fraOgMed = MINUS_YEARS_1.withDayOfMonth(1);
        LocalDate tilOgMed = fraOgMed.plusYears(1);
        return VerdikjedeTestHjelper.lagBehandlingFL(repositoryProvider, resultatRepositoryProvider, scenario, inntektSammenligningsgrunnlag, inntektFrilans, beregningVirksomhet, fraOgMed, tilOgMed);
    }

    private void mockKortvarigArbeidsforhold(LocalDate fomDato, LocalDate tomDato) {
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = YrkesaktivitetBuilder.nyAktivitetsAvtaleBuilder()
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fomDato, tomDato));
        Yrkesaktivitet yrkesaktivitet = YrkesaktivitetBuilder.oppdatere(Optional.empty()).leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder).medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD).build();
        yrkesaktivitetMap.put(beregningsgrunnlagRepository.hentAggregat(behandling).getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0), yrkesaktivitet);
    }

    @Test
    public void skalLageEnPeriode() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        VerdikjedeTestHjelper.opprettInntektsmelding(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1 + 1000));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        verifiserSammenligningsgrunnlag(resultat.getBeregningsgrunnlag().getSammenligningsgrunnlag(), ÅRSINNTEKT1,
            SKJÆRINGSTIDSPUNKT_BEREGNING.minusYears(1).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT_BEREGNING.withDayOfMonth(1).minusDays(1),
            81L);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, (MÅNEDSINNTEKT1 + 1000) * 12, null, null);
    }

    @Test
    public void skalLageEnPeriodeNårNaturalytelseBortfallerPåSkjæringstidspunktet() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        VerdikjedeTestHjelper.opprettInntektsmeldingNaturalytelseBortfaller(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_BEREGNING);

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageToPerioderNaturalYtelseBortfaller() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        VerdikjedeTestHjelper.opprettInntektsmeldingNaturalytelseBortfaller(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), null, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageToPerioderNaturalYtelseTilkommer() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        VerdikjedeTestHjelper.opprettInntektsmeldingNaturalytelseTilkommer(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), null, 1, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, NATURALYTELSE_I_PERIODE_2 * 12);
    }

    @Test
    public void skalLageToPerioderKortvarigArbeidsforhold() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        BeregningsgrunnlagPrStatusOgAndel eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(eksisterendeAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        VerdikjedeTestHjelper.opprettInntektsmelding(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1));
        mockKortvarigArbeidsforhold(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        //Før steget er det ingen inntekter på andelen
        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));
        // Her skulle det vært inntekter på andelen
        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);
        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), null, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);
    }

    @Test
    public void skalLageTrePerioderKortvarigArbeidsforholdOgNaturalYtelse() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        BeregningsgrunnlagPrStatusOgAndel eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(eksisterendeAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        VerdikjedeTestHjelper.opprettInntektsmeldingNaturalytelseBortfaller(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3));
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        mockKortvarigArbeidsforhold(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, NATURALYTELSE_I_PERIODE_2 * 12, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), null, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageEnPeriodeFrilanser() {
        // Arrange
        Beregningsgrunnlag grunnlagFL = lagBeregningsgrunnlagFL(scenario);
        BeregningsgrunnlagPrStatusOgAndel.builder(grunnlagFL.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0))
            .build(grunnlagFL.getBeregningsgrunnlagPerioder().get(0));
        behandling = lagBehandlingFL(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        VerdikjedeTestHjelper.opprettInntektsmelding(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, grunnlagFL);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGFL(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1);
    }

    private void splitBeregningsgrunnlagPeriode(LocalDate nyPeriodeFom, PeriodeÅrsak nyPeriodeÅrsak) {
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        List<BeregningsgrunnlagPeriode> perioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode = perioder.get(perioder.size() - 1);
        if (beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom().equals(nyPeriodeFom)) {
            BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode)
                .leggTilPeriodeÅrsak(nyPeriodeÅrsak);
            beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
            return;
        }
        BeregningsgrunnlagPeriode.builder(beregningsgrunnlagPeriode)
            .medBeregningsgrunnlagPeriode(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom(), nyPeriodeFom.minusDays(1));

        BeregningsgrunnlagPeriode nyBeregningsgrunnlagPeriode = Kopimaskin.deepCopy(beregningsgrunnlagPeriode);
        BeregningsgrunnlagPeriode.builder(nyBeregningsgrunnlagPeriode)
            .medBeregningsgrunnlagPeriode(nyPeriodeFom, null)
            .leggTilPeriodeÅrsak(nyPeriodeÅrsak).build(nyttGrunnlag);
        beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
    }

    @Test
    public void skalLageToPerioderKortvarigArbeidsforholdHvorTomSammenfallerMedBortfallAvNaturalytelse() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        BeregningsgrunnlagPrStatusOgAndel eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(eksisterendeAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        VerdikjedeTestHjelper.opprettInntektsmeldingNaturalytelseBortfaller(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        mockKortvarigArbeidsforhold(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(2);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), null, 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageBeregningsgrunnlagMedTrePerioder() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1 + MÅNEDSINNTEKT2), BigDecimal.valueOf(MÅNEDSINNTEKT1 + MÅNEDSINNTEKT2), beregningVirksomhet1);
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(lagBgAndelArbeidsforhold(ARBEIDSPERIODE_FOM, ARBEIDSPERIODE_TOM, beregningVirksomhet2))
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        VerdikjedeTestHjelper.opprettInntektsmeldingNaturalytelseBortfaller(repositoryProvider, behandling, beregningVirksomhet2, BigDecimal.valueOf(MÅNEDSINNTEKT2),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_2), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2));
        VerdikjedeTestHjelper.opprettInntektsmeldingNaturalytelseBortfaller(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1),
            BigDecimal.valueOf(NATURALYTELSE_I_PERIODE_3), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        Beregningsgrunnlag beregningsgrunnlag = resultat.getBeregningsgrunnlag();
        assertThat(beregningsgrunnlag).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(beregningsgrunnlag.getBeregningsgrunnlagPerioder()).hasSize(3);

        BeregningsgrunnlagPeriode periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).minusDays(1), 2);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), beregningVirksomhet2, ÅRSINNTEKT2, null, null);

        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4).minusDays(1), 2, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), beregningVirksomhet2, ÅRSINNTEKT2, NATURALYTELSE_I_PERIODE_2 * 12, null);

        periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(4), null, 2, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, NATURALYTELSE_I_PERIODE_3 * 12, null);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(1), beregningVirksomhet2, ÅRSINNTEKT2, NATURALYTELSE_I_PERIODE_2 * 12, null);
    }

    @Test
    public void skalLageBeregningsgrunnlagMedTrePerioderKortvarigFørNaturalytelse() {
        // Arrange
        behandling = lagBehandling(scenario, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1), beregningVirksomhet1);
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        BeregningsgrunnlagPrStatusOgAndel eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(eksisterendeAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        splitBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(3), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        mockKortvarigArbeidsforhold(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(2).minusDays(1));

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlagRepository.hentAggregat(behandling));

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(3);

        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2).minusDays(1), 1);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(1);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(2), SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3).minusDays(1), 1, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);

        periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(2);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(3), null, 1, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        verifiserBGAT(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0), beregningVirksomhet1, ÅRSINNTEKT1, null, null);
    }

    @Test
    public void skalGiEittAksjonspunktForSNNyIArbeidslivetOgKortvarigArbeidsforhold() {
        // Arrange
        Beregningsgrunnlag nyttGrunnlag = lagBeregningsgrunnlagATFL_SN(scenario, true).dypKopi();
        behandling = lagBehandlingFor_AT_SN(repositoryProvider, resultatRepositoryProvider, scenario,
            BigDecimal.valueOf(12 * MÅNEDSINNTEKT1), 2014, SKJÆRINGSTIDSPUNKT_BEREGNING,
            beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1), BigDecimal.valueOf(MÅNEDSINNTEKT1));
        BeregningsgrunnlagPrStatusOgAndel eksisterendeAndel = nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        BeregningsgrunnlagPrStatusOgAndel.builder(eksisterendeAndel)
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder(eksisterendeAndel.getBgAndelArbeidsforhold().orElse(null)).medTidsbegrensetArbeidsforhold(true))
            .build(nyttGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        beregningsgrunnlagRepository.lagre(behandling, nyttGrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
        VerdikjedeTestHjelper.opprettInntektsmelding(repositoryProvider, behandling, beregningVirksomhet1, BigDecimal.valueOf(MÅNEDSINNTEKT1));
        mockKortvarigArbeidsforhold(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(1), SKJÆRINGSTIDSPUNKT_OPPTJENING.plusMonths(4).minusDays(1));
        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, nyttGrunnlag);
        // Assert
        Beregningsgrunnlag bg = resultat.getBeregningsgrunnlag();
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        bg.getBeregningsgrunnlagPerioder().forEach(p -> assertThat(p.getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2));
        List<AksjonspunktDefinisjon> aps = resultat.getAksjonspunkter();
        assertThat(aps.stream().filter(a -> a.equals(AksjonspunktDefinisjon.FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET)).count()).isEqualTo(1);
    }

    private void verifiserPeriode(BeregningsgrunnlagPeriode periode, LocalDate fom, LocalDate tom, int antallAndeler, PeriodeÅrsak... forventedePeriodeÅrsaker) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getPeriodeÅrsaker()).containsExactlyInAnyOrder(forventedePeriodeÅrsaker);
    }

    private void verifiserSammenligningsgrunnlag(Sammenligningsgrunnlag sammenligningsgrunnlag, double rapportertPrÅr, LocalDate fom,
                                                 LocalDate tom, long avvikPromille) {
        assertThat(sammenligningsgrunnlag.getRapportertPrÅr().doubleValue()).isEqualTo(rapportertPrÅr);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeFom()).isEqualTo(fom);
        assertThat(sammenligningsgrunnlag.getSammenligningsperiodeTom()).isEqualTo(tom);
        assertThat(sammenligningsgrunnlag.getAvvikPromille()).isEqualTo(avvikPromille);
    }

    private void verifiserBGAT(BeregningsgrunnlagPrStatusOgAndel bgpsa, VirksomhetEntitet virksomhet, double årsinntekt,
                               Double naturalytelseBortfaltPrÅr, Double naturalytelseTilkommerPrÅr) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(bgpsa.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).hasValueSatisfying(virk -> assertThat(virk).isEqualTo(virksomhet));
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.ARBEID);
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
        if (naturalytelseBortfaltPrÅr == null) {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).as("naturalytelseBortfalt").isEmpty();
        } else {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr)).as("naturalytelseBortfalt").hasValueSatisfying(naturalytelse ->
                assertThat(naturalytelse.doubleValue()).isEqualTo(naturalytelseBortfaltPrÅr)
            );
        }
        if (naturalytelseTilkommerPrÅr == null) {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseTilkommetPrÅr)).as("naturalytelseTilkommer").isEmpty();
        } else {
            assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseTilkommetPrÅr)).as("naturalytelseTilkommer").hasValueSatisfying(naturalytelse ->
                assertThat(naturalytelse.doubleValue()).isEqualTo(naturalytelseTilkommerPrÅr)
            );
        }
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
    }

    private void verifiserBGFL(BeregningsgrunnlagPrStatusOgAndel bgpsa, VirksomhetEntitet virksomhet, double årsinntekt) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(bgpsa.getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).hasValueSatisfying(virk -> assertThat(virk).isEqualTo(virksomhet));
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.FRILANS);
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isEqualTo(årsinntekt);
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).as("BruttoPrÅr").isEqualTo(årsinntekt);
        assertThat(bgpsa.getOverstyrtPrÅr()).as("OverstyrtPrÅr").isNull();
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
    }
}
