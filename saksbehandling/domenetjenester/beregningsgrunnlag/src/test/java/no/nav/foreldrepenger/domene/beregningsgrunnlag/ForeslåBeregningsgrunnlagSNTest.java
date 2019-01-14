package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
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
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
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

public class ForeslåBeregningsgrunnlagSNTest {

    private static final double MÅNEDSINNTEKT1 = 12345d;

    private static final double BEREGNINGSGRUNNLAG = 148989.08d;

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MAY, 10);

    private static final LocalDate SKJÆRINGSTIDSPUNKT_BEREGNING = SKJÆRINGSTIDSPUNKT_OPPTJENING;
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);

    private static final AktørId AKTØR_ID = new AktørId("210195");

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());

    private ForeslåBeregningsgrunnlag tjeneste;

    @Mock
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningInntektArbeidYtelseTjeneste;
    @Mock
    private KontrollerFaktaBeregningTjeneste kontrollerFaktaBeregningTjeneste;

    private Behandling behandling;

    private Beregningsgrunnlag beregningsgrunnlag;

    private ScenarioMorSøkerForeldrepenger scenario;

    @Before
    public void setup() {
        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = mock(SkjæringstidspunktTjeneste.class);
        when(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(any())).thenReturn(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, resultatRepositoryProvider, skjæringstidspunktTjeneste);
        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
        IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = mock(IAYRegisterInnhentingFPTjenesteImpl.class);
        when(iayRegisterInnhentingTjeneste.innhentInntekterFor(any(Behandling.class), any(), any(), any()))
            .thenAnswer(a -> repositoryProvider.getInntektArbeidYtelseRepository().opprettBuilderFor(a.getArgument(0), VersjonType.REGISTER));
        HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, inntektArbeidYtelseTjeneste, iayRegisterInnhentingTjeneste);
        MapBeregningsgrunnlagFraVLTilRegel oversetterTilRegel = new MapBeregningsgrunnlagFraVLTilRegel(repositoryProvider, resultatRepositoryProvider, opptjeningInntektArbeidYtelseTjeneste, skjæringstidspunktTjeneste, hentGrunnlagsdataTjeneste, 5);
        MapBeregningsgrunnlagFraRegelTilVL oversetterFraRegel = new MapBeregningsgrunnlagFraRegelTilVL(repositoryProvider, inntektArbeidYtelseTjeneste);
        tjeneste = new ForeslåBeregningsgrunnlag(oversetterTilRegel, oversetterFraRegel, repositoryProvider, kontrollerFaktaBeregningTjeneste, hentGrunnlagsdataTjeneste);
        scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        beregningsgrunnlag = lagBeregningsgrunnlag(scenario);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(ScenarioMorSøkerForeldrepenger scenario) {
        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = scenario.medBeregningsgrunnlag();
        beregningsgrunnlagBuilder.medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_BEREGNING)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP);
        beregningsgrunnlagBuilder.leggTilAktivitetStatus(BeregningsgrunnlagAktivitetStatus.builder()
            .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
        beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT_BEREGNING, null)
            .leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder()
                    .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
                    .medArbeidsperiodeTom(LocalDate.now().plusYears(2)))
                .medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE)));
        return beregningsgrunnlagBuilder.build();
    }

    @Test
    public void testBeregningsgrunnlagSelvstendigNæringsdrivende() {
        // Arrange
        behandling = VerdikjedeTestHjelper.lagBehandlingForSN(repositoryProvider, resultatRepositoryProvider, scenario, BigDecimal.valueOf(12 * MÅNEDSINNTEKT1), 2014);

        // Act
        BeregningsgrunnlagRegelResultat resultat = tjeneste.foreslåBeregningsgrunnlag(behandling, beregningsgrunnlag);

        // Assert
        assertThat(resultat.getBeregningsgrunnlag()).isNotNull();
        assertThat(resultat.getAksjonspunkter()).isEmpty();
        assertThat(resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder()).hasSize(1);
        BeregningsgrunnlagPeriode periode = resultat.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0);
        verifiserPeriode(periode, SKJÆRINGSTIDSPUNKT_BEREGNING, null, 1);
        verifiserBGSN(periode.getBeregningsgrunnlagPrStatusOgAndelList().get(0));
    }

    private void verifiserPeriode(BeregningsgrunnlagPeriode periode, LocalDate fom, LocalDate tom, int antallAndeler) {
        assertThat(periode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(fom);
        assertThat(periode.getBeregningsgrunnlagPeriodeTom()).isEqualTo(tom);
        assertThat(periode.getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(antallAndeler);
        assertThat(periode.getBruttoPrÅr().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG, within(0.01));
        assertThat(periode.getRedusertPrÅr()).isNull();
        assertThat(periode.getAvkortetPrÅr()).isNull();
    }

    private void verifiserBGSN(BeregningsgrunnlagPrStatusOgAndel bgpsa) {
        assertThat(bgpsa.getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(bgpsa.getBeregningsperiodeFom()).isEqualTo(LocalDate.of(2014, Month.JANUARY, 1));
        assertThat(bgpsa.getBeregningsperiodeTom()).isEqualTo(LocalDate.of(2016, Month.DECEMBER, 31));
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getVirksomhet)).isEmpty();
        assertThat(bgpsa.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)).isEmpty();
        assertThat(bgpsa.getArbeidsforholdType()).isEqualTo(OpptjeningAktivitetType.UDEFINERT);
        assertThat(bgpsa.getBeregnetPrÅr().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG, within(0.01));
        assertThat(bgpsa.getBruttoPrÅr().doubleValue()).isCloseTo(BEREGNINGSGRUNNLAG, within(0.01));
        assertThat(bgpsa.getOverstyrtPrÅr()).isNull();
        assertThat(bgpsa.getRedusertPrÅr()).isNull();
        assertThat(bgpsa.getAvkortetPrÅr()).isNull();
    }
}
