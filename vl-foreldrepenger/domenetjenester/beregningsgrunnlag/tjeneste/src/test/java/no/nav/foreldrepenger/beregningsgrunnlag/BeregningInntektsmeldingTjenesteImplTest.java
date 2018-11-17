package no.nav.foreldrepenger.beregningsgrunnlag;

import static no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil.AKTØR_ID;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningIAYTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningInntektsmeldingTestUtil;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningsgrunnlagTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class BeregningInntektsmeldingTjenesteImplTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2018, Month.MARCH, 23);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);

    @Inject
    private BeregningIAYTestUtil iayTestUtil;
    @Inject
    private BeregningInntektsmeldingTestUtil inntektsmeldingTestUtil;

    @Inject
    private BeregningsgrunnlagTestUtil beregningsgrunnlagTestUtil;

    @Inject
    private BeregningInntektsmeldingTjeneste beregningInntektsmeldingTjeneste;

    @Inject
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;
    private Behandling behandling;

    private BigDecimal grunnbeløp;

    @Before
    public void setup() {
        grunnbeløp = beregningsgrunnlagTestUtil.getGrunnbeløp(SKJÆRINGSTIDSPUNKT_OPPTJENING);
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        behandling = scenario.lagre(repositoryProvider);
    }

    @Test
    public void skalReturnereRiktigTotalRefusjonMedEnArbeidsgiverOgRefusjonPåOppgittDato() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        Integer refusjon = 10000;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp(refusjon*12));
    }

    @Test
    public void skalReturnereRiktigTotalRefusjonMedToArbeidsgiverOgRefusjonPåOppgittDato() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 10000;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp(refusjon*12*2));
    }

    @Test
    public void skalReturnereRiktigTotalRefusjonMedToArbeidsgiverOgRefusjonMedUlikMottattDatoOgUlikRefusjonPåOppgittDato() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 43242;
        Integer refusjon2 = 2311;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon + refusjon2)*12));
    }

    @Test
    public void skalReturnereRiktigTotalRefusjonMedToArbeidsgiverOgUlikRefusjonMedOpphørPåOppgittDato() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 43242;
        Integer refusjon2 = 2311;
        LocalDate opphørsdato1 = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusWeeks(10);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato1);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, opphørsdato1.plusDays(1));

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp(refusjon2*12));
    }

    @Test
    public void skalReturnereTrueForTotalRefusjonStørreEnnGrunnbeløp() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 6*grunnbeløp.intValue()/12;
        Integer refusjon2 = 23123;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        boolean erStørreEnnSeksG = beregningInntektsmeldingTjeneste.erTotaltRefusjonskravStørreEnnSeksG(behandling, Beregningsgrunnlag.builder().medRedusertGrunnbeløp(grunnbeløp).medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).build(), SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(erStørreEnnSeksG).isTrue();
    }

    @Test
    public void skalReturnereFalseForTotalRefusjonStørreEnnGrunnbeløp() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        boolean erStørreEnnSeksG = beregningInntektsmeldingTjeneste.erTotaltRefusjonskravStørreEnnSeksG(behandling, Beregningsgrunnlag.builder().medRedusertGrunnbeløp(grunnbeløp).medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT_OPPTJENING).build(), SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(erStørreEnnSeksG).isFalse();
    }

    @Test
    public void skalBeregneRiktigBeløpForInntektsmeldingPåSkjæringstidspunktMedOpphørLikTidenesEnde() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        LocalDate opphørsdato = TIDENES_ENDE;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon2 + refusjon)*12));
    }

    @Test
    public void skalBeregneRiktigBeløpForInntektsmeldingPåTidenesEndeMedOpphørLikTidenesEndeMinus1Dag() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        LocalDate opphørsdato = TIDENES_ENDE.minusDays(1);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, TIDENES_ENDE);

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon2)*12));
    }

    @Test
    public void skalBeregneRiktigBeløpForInntektsmeldingPåTidenesEndeMinus1DagMedOpphørLikTidenesEndeMinus1Dag() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        LocalDate opphørsdato = TIDENES_ENDE.minusDays(1);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, TIDENES_ENDE.minusDays(1));

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon2 + refusjon)*12));
    }

    @Test
    public void skalBeregneRiktigBeløpForInntektsmeldingPåTidenesEndeMinus1DagMedOpphørLikTidenesEnde() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        LocalDate opphørsdato = TIDENES_ENDE;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, TIDENES_ENDE.minusDays(1));

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon2 + refusjon)*12));
    }

    @Test
    public void skalBeregneRiktigBeløpForInntektsmeldingPåSkjæringstidspunktMedOpphørSkjæringstidspunkt() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        LocalDate opphørsdato = SKJÆRINGSTIDSPUNKT_OPPTJENING;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon2 + refusjon)*12));
    }

    @Test
    public void skalBeregneRiktigBeløpForInntektsmeldingPåSkjæringstidspunktPluss1DagMedOpphørSkjæringstidspunkt() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        LocalDate opphørsdato = SKJÆRINGSTIDSPUNKT_OPPTJENING;
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING, arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1));

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon2)*12));
    }

    @Test
    public void skalBeregneRiktigBeløpForInntektsmeldingPåSkjæringstidspunktMedOpphørSkjæringstidspunktPluss1Dag() {
        // Arrange
        String orgnr = "123787422";
        String arbId = "8989";
        String orgnr2 = "187422";
        String arbId2 = "89234289";
        Integer refusjon = 2*grunnbeløp.intValue()/12;
        Integer refusjon2 = 3*grunnbeløp.intValue()/12;
        LocalDate opphørsdato = SKJÆRINGSTIDSPUNKT_OPPTJENING.plusDays(1);
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), arbId, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr));
        iayTestUtil.byggArbeidForBehandling(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING, SKJÆRINGSTIDSPUNKT_OPPTJENING.minusWeeks(12), SKJÆRINGSTIDSPUNKT_OPPTJENING.minusDays(1), arbId2, beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr2));
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr, arbId, SKJÆRINGSTIDSPUNKT_OPPTJENING, Collections.emptyList(), refusjon, opphørsdato);
        inntektsmeldingTestUtil.opprettInntektsmelding(behandling, orgnr2, arbId2, SKJÆRINGSTIDSPUNKT_OPPTJENING, refusjon2);

        // Act
        Beløp totalRefusjon = beregningInntektsmeldingTjeneste.totaltRefusjonsbeløpFraInntektsmelding(behandling, SKJÆRINGSTIDSPUNKT_OPPTJENING);

        // Assert
        assertThat(totalRefusjon).isEqualTo(new Beløp((refusjon2 + refusjon)*12));
    }
}
