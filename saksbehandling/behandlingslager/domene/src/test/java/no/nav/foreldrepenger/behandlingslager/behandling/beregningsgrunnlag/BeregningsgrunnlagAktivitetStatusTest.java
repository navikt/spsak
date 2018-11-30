package no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

public class BeregningsgrunnlagAktivitetStatusTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final AktivitetStatus AKTIVITET_STATUS = AktivitetStatus.ARBEIDSTAKER;
    private static final Hjemmel BARE_ARBEIDSTAKER = Hjemmel.F_14_7_8_30;
    private Beregningsgrunnlag beregningsgrunnlag;
    private BeregningsgrunnlagAktivitetStatus beregningsgrunnlagAktivitetStatus;

    @Before
    public void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
        beregningsgrunnlagAktivitetStatus = lagMedPaakrevdeFelter();
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(beregningsgrunnlagAktivitetStatus.getBeregningsgrunnlag()).isEqualTo(beregningsgrunnlag);
        assertThat(beregningsgrunnlagAktivitetStatus.getAktivitetStatus()).isEqualTo(AKTIVITET_STATUS);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        BeregningsgrunnlagAktivitetStatus.Builder builder = BeregningsgrunnlagAktivitetStatus.builder();
        try {
            builder.build(null);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlag");
        }

        builder = BeregningsgrunnlagAktivitetStatus.builder();

        try {
            builder.build(beregningsgrunnlag);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("aktivitetStatus");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo(null);
        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo("blabla");
        assertThat(beregningsgrunnlagAktivitetStatus).isEqualTo(beregningsgrunnlagAktivitetStatus);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        BeregningsgrunnlagAktivitetStatus beregningsgrunnlagAktivitetStatus2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlagAktivitetStatus).isEqualTo(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus2).isEqualTo(beregningsgrunnlagAktivitetStatus);
        assertThat(beregningsgrunnlagAktivitetStatus.hashCode()).isEqualTo(beregningsgrunnlagAktivitetStatus2.hashCode());
        assertThat(beregningsgrunnlagAktivitetStatus2.hashCode()).isEqualTo(beregningsgrunnlagAktivitetStatus.hashCode());

        beregningsgrunnlagAktivitetStatus2 = lagBuilderMedPaakrevdeFelter().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE).build(beregningsgrunnlag);
        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus2).isNotEqualTo(beregningsgrunnlagAktivitetStatus);
        assertThat(beregningsgrunnlagAktivitetStatus.hashCode()).isNotEqualTo(beregningsgrunnlagAktivitetStatus2.hashCode());
        assertThat(beregningsgrunnlagAktivitetStatus2.hashCode()).isNotEqualTo(beregningsgrunnlagAktivitetStatus.hashCode());
    }

    @Test
    public void skal_bruke_aktivitetstatus_i_equalsOgHashCode() {
        BeregningsgrunnlagAktivitetStatus beregningsgrunnlagAktivitetStatus2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlagAktivitetStatus).isEqualTo(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus.hashCode()).isEqualTo(beregningsgrunnlagAktivitetStatus2.hashCode());

        beregningsgrunnlagAktivitetStatus2 = lagBuilderMedPaakrevdeFelter().medAktivitetStatus(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE).build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus.hashCode()).isNotEqualTo(beregningsgrunnlagAktivitetStatus2.hashCode());
    }

    @Test
    public void skal_bruke_beregningsgrunnlag_i_equalsOgHashCode() {
        BeregningsgrunnlagAktivitetStatus beregningsgrunnlagAktivitetStatus2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlagAktivitetStatus).isEqualTo(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus.hashCode()).isEqualTo(beregningsgrunnlagAktivitetStatus2.hashCode());

        Beregningsgrunnlag beregningsgrunnlag2 = lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate.now().plusDays(1));
        BeregningsgrunnlagAktivitetStatus.Builder builder = lagBuilderMedPaakrevdeFelter();
        beregningsgrunnlagAktivitetStatus2 = builder.build(beregningsgrunnlag2);

        assertThat(beregningsgrunnlagAktivitetStatus).isNotEqualTo(beregningsgrunnlagAktivitetStatus2);
        assertThat(beregningsgrunnlagAktivitetStatus.hashCode()).isNotEqualTo(beregningsgrunnlagAktivitetStatus2.hashCode());
    }

    private BeregningsgrunnlagAktivitetStatus lagMedPaakrevdeFelter() {
        return lagBuilderMedPaakrevdeFelter().build(beregningsgrunnlag);
    }

    private BeregningsgrunnlagAktivitetStatus.Builder lagBuilderMedPaakrevdeFelter() {
        return BeregningsgrunnlagAktivitetStatus.builder()
            .medHjemmel(BARE_ARBEIDSTAKER)
            .medAktivitetStatus(AKTIVITET_STATUS);
    }

    private static Beregningsgrunnlag lagBeregningsgrunnlag() {
        return lagBeregningsgrunnlagMedSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    private static Beregningsgrunnlag lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return Beregningsgrunnlag.builder().medSkjæringstidspunkt(skjæringstidspunkt).build();
    }
}
