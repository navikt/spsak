package no.nav.foreldrepenger.behandlingslager.økonomioppdrag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

public class Avstemming115EntityTest {
    private Avstemming115.Builder avstemming115Builder;
    private Avstemming115 avstemming115;
    private Avstemming115 avstemming115_2;

    private static final String KODEKOMPONENT = "1234";
    private static final LocalDateTime NOKKELAVSTEMMMING = LocalDateTime.now().minusDays(2);
    private static final LocalDateTime TIDSPNKTMELDING = LocalDateTime.now().minusDays(1);

    private static final String FORVENTET_EXCEPTION = "forventet exception";

    @Before
    public void setup() {
        avstemming115Builder = Avstemming115.builder();
        avstemming115 = null;
    }


    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        avstemming115 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(avstemming115.getKodekomponent()).isEqualTo(KODEKOMPONENT);
        assertThat(avstemming115.getNokkelAvstemming()).isEqualTo(NOKKELAVSTEMMMING);
        assertThat(avstemming115.getTidspnktMelding()).isEqualTo(TIDSPNKTMELDING);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {

        //mangler kodeKomponent
        try {
            avstemming115Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("kodekomponent");
        }

        //mangler nokkelAvstemming
        avstemming115Builder.medKodekomponent(KODEKOMPONENT);
        try {
            avstemming115Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("nokkelAvstemming");
        }

        //mangler tidspnktMelding
        avstemming115Builder.medNokkelAvstemming(NOKKELAVSTEMMMING);
        try {
            avstemming115Builder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("tidspnktMelding");
        }


    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        avstemming115 = lagBuilderMedPaakrevdeFelter().build();

        assertThat(avstemming115).isNotNull();
        assertThat(avstemming115).isNotEqualTo("blabla");
        assertThat(avstemming115).isEqualTo(avstemming115);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        avstemming115Builder = lagBuilderMedPaakrevdeFelter();
        avstemming115 = avstemming115Builder.build();
        avstemming115_2 = avstemming115Builder.build();

        assertThat(avstemming115).isEqualTo(avstemming115_2);
        assertThat(avstemming115_2).isEqualTo(avstemming115);

        avstemming115_2 = avstemming115Builder.medKodekomponent("1235").build();
        assertThat(avstemming115).isNotEqualTo(avstemming115_2);
        assertThat(avstemming115_2).isNotEqualTo(avstemming115);
    }


    @Test
    public void skal_bruke_Kodekomponent_i_equalsOgHashCode() {
        avstemming115Builder = lagBuilderMedPaakrevdeFelter();
        avstemming115 = avstemming115Builder.build();
        avstemming115Builder.medKodekomponent("1236");
        avstemming115_2 = avstemming115Builder.build();

        assertThat(avstemming115).isNotEqualTo(avstemming115_2);
        assertThat(avstemming115.hashCode()).isNotEqualTo(avstemming115_2.hashCode());

    }

    @Test
    public void skal_bruke_NokkelAvstemming_i_equalsOgHashCode() {
        avstemming115Builder = lagBuilderMedPaakrevdeFelter();
        avstemming115 = avstemming115Builder.build();
        avstemming115Builder.medNokkelAvstemming(LocalDateTime.now().minusDays(10));
        avstemming115_2 = avstemming115Builder.build();

        assertThat(avstemming115).isNotEqualTo(avstemming115_2);
        assertThat(avstemming115.hashCode()).isNotEqualTo(avstemming115_2.hashCode());

    }

    @Test
    public void skal_bruke_TidspnktMelding_i_equalsOgHashCode() {
        avstemming115Builder = lagBuilderMedPaakrevdeFelter();
        avstemming115 = avstemming115Builder.build();
        avstemming115Builder.medTidspnktMelding(LocalDateTime.now().minusDays(5));
        avstemming115_2 = avstemming115Builder.build();

        assertThat(avstemming115).isNotEqualTo(avstemming115_2);
        assertThat(avstemming115.hashCode()).isNotEqualTo(avstemming115_2.hashCode());

    }


    private Avstemming115.Builder lagBuilderMedPaakrevdeFelter(){
        return Avstemming115.builder()
                .medKodekomponent(KODEKOMPONENT)
                .medNokkelAvstemming(NOKKELAVSTEMMMING)
                .medTidspnktMelding(TIDSPNKTMELDING);
    }

}

