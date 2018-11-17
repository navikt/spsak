package no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

public class BeregningsgrunnlagTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();

    private Beregningsgrunnlag.Builder builder;
    private Beregningsgrunnlag beregningsgrunnlag;

    @Before
    public void setup() {
        beregningsgrunnlag = lagMedPaakrevdeFelter();
        builder = lagBuilderMedPaakrevdeFelter();
    }

    private static Beregningsgrunnlag lagMedPaakrevdeFelter() {
        return lagBuilderMedPaakrevdeFelter().build();
    }

    private static Beregningsgrunnlag.Builder lagBuilderMedPaakrevdeFelter() {
        return Beregningsgrunnlag.builder().medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        Beregningsgrunnlag beregningsgrunnlag = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag.getSkjæringstidspunkt()).isEqualTo(SKJÆRINGSTIDSPUNKT);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        Beregningsgrunnlag.Builder builder = Beregningsgrunnlag.builder();
        try {
            builder.build();
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("skjæringstidspunkt");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        Beregningsgrunnlag beregningsgrunnlag = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isNotEqualTo(null);
        assertThat(beregningsgrunnlag).isNotEqualTo("blabla");
        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        Beregningsgrunnlag beregningsgrunnlag2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag2).isEqualTo(beregningsgrunnlag);

        builder.medSkjæringstidspunkt(LocalDate.now().plusDays(1));
        beregningsgrunnlag2 = builder.build();
        assertThat(beregningsgrunnlag).isNotEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag2).isNotEqualTo(beregningsgrunnlag);
    }

    @Test
    public void skal_bruke_skjaeringstidspunkt_i_equalsOgHashCode() {
        Beregningsgrunnlag beregningsgrunnlag2 = lagMedPaakrevdeFelter();

        assertThat(beregningsgrunnlag).isEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag.hashCode()).isEqualTo(beregningsgrunnlag2.hashCode());

        builder.medSkjæringstidspunkt(LocalDate.now().plusDays(1));
        beregningsgrunnlag2 = builder.build();

        assertThat(beregningsgrunnlag).isNotEqualTo(beregningsgrunnlag2);
        assertThat(beregningsgrunnlag.hashCode()).isNotEqualTo(beregningsgrunnlag2.hashCode());
    }
}
