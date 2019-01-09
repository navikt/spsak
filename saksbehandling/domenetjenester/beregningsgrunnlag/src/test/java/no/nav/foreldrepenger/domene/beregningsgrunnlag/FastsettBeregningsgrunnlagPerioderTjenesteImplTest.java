package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.RefusjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.PeriodeÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningOpptjeningTestUtil;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.spsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.Tid;

@RunWith(CdiRunner.class)
public class FastsettBeregningsgrunnlagPerioderTjenesteImplTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000L);
    private static final String ORG_NUMMER = "45345";
    private static final String ORG_NUMMER_2 = "15345";
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(entityManager);
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    @Inject
    private FastsettBeregningsgrunnlagPerioderTjenesteImpl tjeneste;

    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningInntektsmeldingTestUtil inntektsmeldingTestUtil;
    @Inject
    private BeregningsgrunnlagTestUtil beregningTestUtil;
    @Inject
    private BeregningOpptjeningTestUtil opptjeningTestUtil;
    @Inject
    private BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil;

    private Behandling behandling;

    @Before
    public void setUp() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(AKTØR_ID));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT);
    }

    @Test
    public void ikkeLagPeriodeForRefusjonHvisKunEnInntektsmeldingIngenEndringIRefusjon() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        BigDecimal inntekt = BigDecimal.valueOf(23987);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt);

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, null);
    }

    @Test
    public void lagPeriodeForNaturalytelseTilkommer() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelse naturalYtelseTilkommer = new NaturalYtelseEntitet(SKJÆRINGSTIDSPUNKT.plusDays(30), Tid.TIDENES_ENDE, BigDecimal.valueOf(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        inntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null, naturalYtelseTilkommer);

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(29));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(30), null, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
    }

    @Test
    public void lagPeriodeForNaturalytelseBortfalt() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelse naturalYtelseBortfall = new NaturalYtelseEntitet(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusDays(30), BigDecimal.valueOf(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        inntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null, naturalYtelseBortfall);

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(30));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(31), null, PeriodeÅrsak.NATURALYTELSE_BORTFALT);
    }

    @Test
    public void lagPerioderForNaturalytelseBortfaltOgTilkommer() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        BigDecimal inntekt = BigDecimal.valueOf(40000);
        NaturalYtelse naturalYtelseBortfalt = new NaturalYtelseEntitet(SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusDays(30), BigDecimal.valueOf(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        NaturalYtelse naturalYtelseTilkommer = new NaturalYtelseEntitet(SKJÆRINGSTIDSPUNKT.plusDays(90), Tid.TIDENES_ENDE, BigDecimal.valueOf(350), NaturalYtelseType.ELEKTRISK_KOMMUNIKASJON);
        inntektsmeldingTestUtil.opprettInntektsmeldingMedNaturalYtelser(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, null, naturalYtelseBortfalt, naturalYtelseTilkommer);

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(3);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(30));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(31), SKJÆRINGSTIDSPUNKT.plusDays(89), PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusDays(90), null, PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
    }

    @Test
    public void lagPeriodeForRefusjonOpphører() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        BigDecimal inntekt = BigDecimal.valueOf(40000);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, inntekt, inntekt, SKJÆRINGSTIDSPUNKT.plusDays(100));

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(100));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusDays(101), null, PeriodeÅrsak.REFUSJON_OPPHØRER);
    }

    @Test
    public void lagPeriodeForGraderingOver6G() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        BigDecimal inntekt2 = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        DatoIntervallEntitet graderingsperiode = DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.plusWeeks(6), Tid.TIDENES_ENDE);
        Gradering gradering = new GraderingEntitet(graderingsperiode, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, "90000", SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        inntektsmeldingTestUtil.opprettInntektsmeldingMedGradering(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav2, inntekt2, gradering);

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(6), null, PeriodeÅrsak.GRADERING);
    }


    @Test
    public void lagPeriodeForGraderingArbeidsforholdTilkomEtterStp() {
        String arbId = "123213";
        // Arrange
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(1), SKJÆRINGSTIDSPUNKT.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER), BigDecimal.TEN);
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Gradering gradering = new GraderingEntitet(SKJÆRINGSTIDSPUNKT.plusWeeks(1), Tid.TIDENES_ENDE, BigDecimal.valueOf(50));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, arbId, SKJÆRINGSTIDSPUNKT, singletonList(gradering), 0, 10);

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(1).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(1), null, PeriodeÅrsak.GRADERING);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(0);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(1);

    }

    @Test
    public void lagPeriodeForRefusjonArbeidsforholdTilkomEtterStp() {
        String arbId = "123213";
        // Arrange

        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(1), SKJÆRINGSTIDSPUNKT.plusMonths(5).minusDays(2), arbId,arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER), BigDecimal.TEN);

        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000));

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, null);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(0);
    }

    @Test
    public void lagPeriodeForRefusjonArbeidsforholdTilkomEtterStpFlerePerioder() {
        // Arrange
        String arbId = "123213";
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(1), SKJÆRINGSTIDSPUNKT.plusMonths(5).minusDays(2), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER_2));

        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(30000), BigDecimal.valueOf(30000), SKJÆRINGSTIDSPUNKT.plusWeeks(2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER_2, SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(20000), BigDecimal.valueOf(20000));

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusWeeks(2));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusWeeks(2).plusDays(1), null, PeriodeÅrsak.REFUSJON_OPPHØRER);
        assertThat(perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(0);
        assertThat(perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList()).hasSize(0);
    }

    @Test
    public void lagPeriodeForGraderingOver6GOgOpphørRefusjonSammeDag() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        opptjeningTestUtil.leggTilOpptjening(behandling, SKJÆRINGSTIDSPUNKT);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        BigDecimal inntekt2 = BigDecimal.valueOf(40000);
        BigDecimal refusjonskrav2 = BigDecimal.ZERO;
        DatoIntervallEntitet graderingsperiode = DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.plusWeeks(6), Tid.TIDENES_ENDE);
        Gradering gradering = new GraderingEntitet(graderingsperiode, BigDecimal.valueOf(50));
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, "80000", SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, "90000", SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato);
        inntektsmeldingTestUtil.opprettInntektsmeldingMedGradering(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav2, inntekt2, gradering);

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, refusjonOpphørerDato);
        assertBeregningsgrunnlagPeriode(perioder.get(1), refusjonOpphørerDato.plusDays(1), null, PeriodeÅrsak.GRADERING, PeriodeÅrsak.REFUSJON_OPPHØRER);
    }


    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktUtenOpphørsdato() {
        // Arrange
        String arbId = "123213";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER));
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        Beregningsgrunnlag beregningsgrunnlag = beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT, berPerioder);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1);

        // Act
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlag.dypKopi();
        tjeneste.fastsettPerioder(behandling, nyttGrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        List<BeregningsgrunnlagPrStatusOgAndel> andeler = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andeler).hasSize(1);
        assertThat(andeler.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(refusjonskrav1.multiply(BigDecimal.valueOf(12)));
    }


    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraSkjæringstidspunktMedOpphørsdato() {
        // Arrange
        String arbId = "123213";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER));
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        Beregningsgrunnlag beregningsgrunnlag = beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT, berPerioder);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato);

        // Act
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlag.dypKopi();
        tjeneste.fastsettPerioder(behandling, nyttGrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndel> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(refusjonskrav1.multiply(BigDecimal.valueOf(12)));

        assertThat(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(refusjonOpphørerDato.plusDays(1));
        List<BeregningsgrunnlagPrStatusOgAndel> andelerIAndrePeriode = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).hasSize(1);
        assertThat(andelerIAndrePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraEtterSkjæringstidspunktMedOpphørsdato() {
        // Arrange
        String arbId = "123213";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER));
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        Beregningsgrunnlag beregningsgrunnlag = beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT, berPerioder);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusWeeks(6).minusDays(1);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, refusjonOpphørerDato);

        // Act
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlag.dypKopi();
        tjeneste.fastsettPerioder(behandling, nyttGrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndel> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(refusjonskrav1.multiply(BigDecimal.valueOf(12)));
        assertThat(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).isEqualTo(refusjonOpphørerDato.plusDays(1));
        assertThat(perioder.get(1).getPeriodeÅrsaker()).isEqualTo(singletonList(PeriodeÅrsak.REFUSJON_OPPHØRER));
        List<BeregningsgrunnlagPrStatusOgAndel> andelerIAndrePeriode = perioder.get(1).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIAndrePeriode).hasSize(1);
        assertThat(andelerIAndrePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    public void skalSetteRefusjonskravForSøktRefusjonFraEtterSkjæringstidspunktUtenOpphørsdato() {
        // Arrange
        String arbId = "123213";
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER));
        List<LocalDateInterval> berPerioder = singletonList(new LocalDateInterval(SKJÆRINGSTIDSPUNKT, null));
        Beregningsgrunnlag beregningsgrunnlag = beregningTestUtil.lagGjeldendeBeregningsgrunnlag(behandling, SKJÆRINGSTIDSPUNKT, berPerioder);

        BigDecimal inntekt1 = BigDecimal.valueOf(90000);
        BigDecimal refusjonskrav1 = inntekt1;
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, ORG_NUMMER, SKJÆRINGSTIDSPUNKT, refusjonskrav1, inntekt1, null);

        // Act
        Beregningsgrunnlag nyttGrunnlag = beregningsgrunnlag.dypKopi();
        tjeneste.fastsettPerioder(behandling, nyttGrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = nyttGrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).isEqualTo(SKJÆRINGSTIDSPUNKT);
        List<BeregningsgrunnlagPrStatusOgAndel> andelerIFørstePeriode = perioder.get(0).getBeregningsgrunnlagPrStatusOgAndelList();
        assertThat(andelerIFørstePeriode).hasSize(1);
        assertThat(andelerIFørstePeriode.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(refusjonskrav1.multiply(BigDecimal.valueOf(12)));
    }

    @Test
    public void skalTesteEndringIRefusjon() {
        // Arrange
        String arbId = "123";
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        SykefraværBuilder builderb = scenario.getSykefraværBuilder();
        SykefraværPeriodeBuilder sykemeldingBuilder = builderb.periodeBuilder();
        sykemeldingBuilder.medPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusDays(36))
            .medArbeidsgiver(Arbeidsgiver.person(AKTØR_ID));
        builderb.leggTil(sykemeldingBuilder);
        scenario.medSykefravær(builderb);
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BigDecimal inntekt = BigDecimal.valueOf(40000);
        List<Refusjon> refusjonsListe = Arrays.asList(
            new RefusjonEntitet(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT.plusMonths(3)),
            new RefusjonEntitet(BigDecimal.valueOf(10000), SKJÆRINGSTIDSPUNKT.plusMonths(6)));
        LocalDate refusjonOpphørerDato = SKJÆRINGSTIDSPUNKT.plusMonths(9).minusDays(1);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT.minusDays(1), SKJÆRINGSTIDSPUNKT.minusYears(2), SKJÆRINGSTIDSPUNKT.plusYears(5), arbId, arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORG_NUMMER));
        Inntektsmelding inntektsmelding = inntektsmeldingTestUtil.opprettInntektsmeldingMedEndringerIRefusjon(behandling, ORG_NUMMER, arbId, SKJÆRINGSTIDSPUNKT, inntekt,
            inntekt, refusjonOpphørerDato, refusjonsListe);
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold.builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(new VirksomhetEntitet.Builder().medOrgnr(ORG_NUMMER).oppdatertOpplysningerNå().build()))
                .medArbforholdRef(inntektsmelding.getArbeidsforholdRef().getReferanse());

            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .build(periode);
        });

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);

        // Assert
        List<BeregningsgrunnlagPeriode> perioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        assertThat(perioder).hasSize(4);
        assertBeregningsgrunnlagPeriode(perioder.get(0), SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(3).minusDays(1));
        assertBeregningsgrunnlagPeriode(perioder.get(1), SKJÆRINGSTIDSPUNKT.plusMonths(3), SKJÆRINGSTIDSPUNKT.plusMonths(6).minusDays(1), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(2), SKJÆRINGSTIDSPUNKT.plusMonths(6), refusjonOpphørerDato, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        assertBeregningsgrunnlagPeriode(perioder.get(3), refusjonOpphørerDato.plusDays(1), null, PeriodeÅrsak.REFUSJON_OPPHØRER);
        Map<LocalDate, BeregningsgrunnlagPrStatusOgAndel> andeler = perioder.stream()
            .collect(Collectors.toMap(BeregningsgrunnlagPeriode::getBeregningsgrunnlagPeriodeFom, p -> p.getBeregningsgrunnlagPrStatusOgAndelList().get(0)));
        assertThat(andeler.get(perioder.get(0).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(inntekt.multiply(BigDecimal.valueOf(12)));
        assertThat(andeler.get(perioder.get(1).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(BigDecimal.valueOf(20000 * 12));
        assertThat(andeler.get(perioder.get(2).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(BigDecimal.valueOf(10000 * 12));
        assertThat(andeler.get(perioder.get(3).getBeregningsgrunnlagPeriodeFom()).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skalKasteFeilHvisAntallPerioderErMerEnn1() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        BeregningsgrunnlagPeriode.Builder periode1 = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT);
        BeregningsgrunnlagPeriode.Builder periode2 = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT.plusDays(1), null);
        Beregningsgrunnlag beregningsgrunnlag = scenario.medBeregningsgrunnlag()
            .medDekningsgrad(100L)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .leggTilBeregningsgrunnlagPeriode(periode1)
            .leggTilBeregningsgrunnlagPeriode(periode2)
            .build();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        // Assert
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("periode, fant 2");

        // Act
        tjeneste.fastsettPerioder(behandling, beregningsgrunnlag);
    }

    private void assertBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, LocalDate expectedFom, LocalDate expectedTom, PeriodeÅrsak... perioderÅrsaker) {
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).as("fom").isEqualTo(expectedFom);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeTom()).as("tom").isEqualTo(expectedTom);
        assertThat(beregningsgrunnlagPeriode.getPeriodeÅrsaker()).as("periodeÅrsaker").containsExactlyInAnyOrder(perioderÅrsaker);
    }

    private BeregningsgrunnlagPeriode.Builder lagBeregningsgrunnlagPerioderBuilder(LocalDate fom, LocalDate tom) {
        return BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(fom, tom);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag() {
        BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder = lagBeregningsgrunnlagPerioderBuilder(SKJÆRINGSTIDSPUNKT, null);
        return lagBeregningsgrunnlag(beregningsgrunnlagPeriodeBuilder);
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(BeregningsgrunnlagPeriode.Builder... beregningsgrunnlagPeriodeBuilders) {
        Beregningsgrunnlag.Builder beregningsgrunnlagBuilder = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP);
        for (BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder : beregningsgrunnlagPeriodeBuilders) {
            beregningsgrunnlagBuilder.leggTilBeregningsgrunnlagPeriode(beregningsgrunnlagPeriodeBuilder);
        }
        return beregningsgrunnlagBuilder.build();
    }
}
