package no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class BeregningsgrunnlagPeriodeTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final LocalDate PERIODE_FOM = LocalDate.now();

    private Beregningsgrunnlag beregningsgrunnlag;
    private List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelListe;
    private BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;

    @Before
    public void setup() {
        beregningsgrunnlag = lagBeregningsgrunnlag();
        beregningsgrunnlagPrStatusOgAndelListe = new ArrayList<>();
        beregningsgrunnlagPrStatusOgAndelListe.add(lagBeregningsgrunnlagPrStatusOgAndel());
        beregningsgrunnlagPeriode = lagMedPaakrevdeFelter(beregningsgrunnlag);
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlag()).isEqualTo(beregningsgrunnlag);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList()).isEqualTo(beregningsgrunnlagPrStatusOgAndelListe);
        assertThat(beregningsgrunnlagPeriode.getBeregningsgrunnlagPeriodeFom()).isEqualTo(PERIODE_FOM);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder = BeregningsgrunnlagPeriode.builder();
        try {
            beregningsgrunnlagPeriodeBuilder.build(null);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlag");
        }

        beregningsgrunnlagPeriodeBuilder.leggTillBeregningsgrunnlagPrStatusOgAndeler(beregningsgrunnlagPrStatusOgAndelListe);

        try {
            beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlag);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlagPeriodeFom");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(null);
        assertThat(beregningsgrunnlagPeriode).isNotEqualTo("blabla");
        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode2 = lagMedPaakrevdeFelter(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode2).isEqualTo(beregningsgrunnlagPeriode);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isEqualTo(beregningsgrunnlagPeriode2.hashCode());
        assertThat(beregningsgrunnlagPeriode2.hashCode()).isEqualTo(beregningsgrunnlagPeriode.hashCode());

        BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder = lagBuilderMedPaakrevdeFelter();
        beregningsgrunnlagPeriodeBuilder.medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(1), null);
        beregningsgrunnlagPeriode2 = beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlag);
        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode2).isNotEqualTo(beregningsgrunnlagPeriode);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode2.hashCode());
        assertThat(beregningsgrunnlagPeriode2.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode.hashCode());
    }

    @Test
    public void skal_bruke_beregningsgrunnlagPeriodeFom_i_equalsOgHashCode() {
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode2 = lagMedPaakrevdeFelter(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isEqualTo(beregningsgrunnlagPeriode2.hashCode());

        BeregningsgrunnlagPeriode.Builder beregningsgrunnlagPeriodeBuilder = lagBuilderMedPaakrevdeFelter();
        beregningsgrunnlagPeriodeBuilder.medBeregningsgrunnlagPeriode(LocalDate.now().minusDays(1), null);
        beregningsgrunnlagPeriode2 = beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode2.hashCode());
    }

    @Test
    public void skal_bruke_beregningsgrunnlag_i_equalsOgHashCode() {
        BeregningsgrunnlagPeriode.Builder builder = lagBuilderMedPaakrevdeFelter();
        BeregningsgrunnlagPeriode beregningsgrunnlagPeriode2 = builder.build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isEqualTo(beregningsgrunnlagPeriode2.hashCode());

        builder.medBeregningsgrunnlagPeriode(LocalDate.now().plusDays(1), null);
        beregningsgrunnlagPeriode2 = builder.build(beregningsgrunnlag);

        assertThat(beregningsgrunnlagPeriode).isNotEqualTo(beregningsgrunnlagPeriode2);
        assertThat(beregningsgrunnlagPeriode.hashCode()).isNotEqualTo(beregningsgrunnlagPeriode2.hashCode());
    }

    private BeregningsgrunnlagPeriode lagMedPaakrevdeFelter(Beregningsgrunnlag beregningsgrunnlag) {
        return lagBuilderMedPaakrevdeFelter().build(beregningsgrunnlag);
    }

    private BeregningsgrunnlagPeriode.Builder lagBuilderMedPaakrevdeFelter() {
        return BeregningsgrunnlagPeriode.builder()
            .leggTillBeregningsgrunnlagPrStatusOgAndeler(beregningsgrunnlagPrStatusOgAndelListe)
            .medBeregningsgrunnlagPeriode(PERIODE_FOM, null);
    }

    private static Beregningsgrunnlag lagBeregningsgrunnlag() {
        return lagBeregningsgrunnlagMedSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    private static Beregningsgrunnlag lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return Beregningsgrunnlag.builder().medSkjæringstidspunkt(skjæringstidspunkt).build();
    }

    private BeregningsgrunnlagPrStatusOgAndel lagBeregningsgrunnlagPrStatusOgAndel() {
        return new BeregningsgrunnlagPrStatusOgAndel();
    }
}
