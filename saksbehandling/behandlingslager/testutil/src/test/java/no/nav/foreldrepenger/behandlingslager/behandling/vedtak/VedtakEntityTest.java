package no.nav.foreldrepenger.behandlingslager.behandling.vedtak;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.Test;

public class VedtakEntityTest {

    private BehandlingVedtak.Builder vedtakBuilder;
    private BehandlingVedtak vedtak;
    private BehandlingVedtak vedtak2;

    private static final LocalDate VEDTAKSDATO = LocalDate.now();
    private static final String ANSVARLIG_SAKSBEHBANDLER = "Ola Normann";
    private static final VedtakResultatType VEDTAK_RESULTAT_TYPE = VedtakResultatType.INNVILGET;
    private static final String FORVENTET_EXCEPTION = "forventet exception";

    @Before
    public void setup() {
        vedtakBuilder = BehandlingVedtak.builder();
        vedtak = null;
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        vedtak = lagBuilderMedPaakrevdeFelter().build();

        assertThat(vedtak.getVedtaksdato(), is(VEDTAKSDATO));
        assertThat(vedtak.getAnsvarligSaksbehandler(), is(ANSVARLIG_SAKSBEHBANDLER));
        assertThat(vedtak.getVedtakResultatType(), is(VEDTAK_RESULTAT_TYPE));
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {

        // mangler vedtaksdato
        try {
            vedtakBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), containsString("vedtaksdato"));
        }

        // mangler ansvarligSaksbehandler
        vedtakBuilder.medVedtaksdato(VEDTAKSDATO);
        try {
            vedtakBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), containsString("ansvarligSaksbehandler"));
        }

        // mangler vedtakResultatType
        vedtakBuilder.medAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHBANDLER);
        try {
            vedtakBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), containsString("vedtakResultatType"));
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        vedtak = lagBuilderMedPaakrevdeFelter().build();

        assertThat(vedtak, is(notNullValue()));
        assertThat(vedtak, is(not("blabla")));
        assertThat(vedtak, is(equalTo(vedtak)));
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        vedtakBuilder = lagBuilderMedPaakrevdeFelter();
        vedtak = vedtakBuilder.build();
        vedtak2 = vedtakBuilder.build();

        assertThat(vedtak, is(equalTo(vedtak2)));
        assertThat(vedtak2, is(equalTo(vedtak)));

        vedtakBuilder.medAnsvarligSaksbehandler("Kari Larsen");
        vedtak2 = vedtakBuilder.build();
        assertThat(vedtak2, is(not(equalTo(vedtak))));
        assertThat(vedtak, is(not(equalTo(vedtak2))));
    }

    @Test
    public void skal_bruke_vedtaksdato_i_equalsOgHashCode() {
        vedtakBuilder = lagBuilderMedPaakrevdeFelter();
        vedtak = vedtakBuilder.build();
        vedtakBuilder.medVedtaksdato(LocalDate.now().plus(1, ChronoUnit.DAYS));
        vedtak2 = vedtakBuilder.build();

        assertThat(vedtak, is(not(equalTo(vedtak2))));
        assertThat(vedtak.hashCode(), is(not(equalTo(vedtak2.hashCode()))));
    }

    @Test
    public void skal_bruke_ansvarligSaksbehandler_i_equalsOgHashCode() {
        vedtakBuilder = lagBuilderMedPaakrevdeFelter();
        vedtak = vedtakBuilder.build();
        vedtakBuilder.medAnsvarligSaksbehandler("Jostein Hansen");
        vedtak2 = vedtakBuilder.build();

        assertThat(vedtak, is(not(equalTo(vedtak2))));
        assertThat(vedtak.hashCode(), is(not(equalTo(vedtak2.hashCode()))));
    }

    @Test
    public void skal_bruke_vedtakResultatType_i_equalsOgHashCode() {
        vedtakBuilder = lagBuilderMedPaakrevdeFelter();
        vedtak = vedtakBuilder.build();
        vedtakBuilder.medVedtakResultatType(VedtakResultatType.AVSLAG);
        vedtak2 = vedtakBuilder.build();

        assertThat(vedtak, is(not(equalTo(vedtak2))));
        assertThat(vedtak.hashCode(), is(not(equalTo(vedtak2.hashCode()))));
    }

    //----------------------------------------------------------------

    private static BehandlingVedtak.Builder lagBuilderMedPaakrevdeFelter() {
        return BehandlingVedtak.builder()
            .medVedtaksdato(VEDTAKSDATO)
            .medAnsvarligSaksbehandler(ANSVARLIG_SAKSBEHBANDLER)
            .medVedtakResultatType(VEDTAK_RESULTAT_TYPE);
    }

}
