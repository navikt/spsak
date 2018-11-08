package no.nav.foreldrepenger.vedtakslager;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtak;
import no.nav.foreldrepenger.behandlingslager.lagretvedtak.LagretVedtakType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class LagretVedtakEntityTest {
    private LagretVedtak.Builder lagretVedtakBuilder;
    private LagretVedtak lagretVedtak;
    private LagretVedtak lagretVedtak2;

    private static final Long FAGSAK_ID = 22L;
    private static final Long BEHANDLING_ID = 433L;
    private static final String STRING_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><element>test av xml</element>";
    private static final LagretVedtakType LAGRET_VEDTAK_TYPE = LagretVedtakType.ADOPSJON;
    private static final String FORVENTET_EXCEPTION = "forventet exception";

    @Before
    public void setup() {
        lagretVedtakBuilder = LagretVedtak.builder();
        lagretVedtak = null;
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        lagretVedtak = lagBuilderMedPaakrevdeFelter().build();

        assertThat(lagretVedtak.getFagsakId()).isEqualTo(FAGSAK_ID);
        assertThat(lagretVedtak.getBehandlingId()).isEqualTo(BEHANDLING_ID);
        assertThat(lagretVedtak.getXmlClob()).isEqualTo(STRING_XML);
        assertThat(lagretVedtak.getLagretVedtakType()).isEqualTo(LAGRET_VEDTAK_TYPE);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {

        // mangler fagsakId
        try {
            lagretVedtakBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("fagsakId");
        }

        // mangler behandlingId
        lagretVedtakBuilder.medFagsakId(FAGSAK_ID);
        try {
            lagretVedtakBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("behandlingId");
        }

        // mangler behandlingId
        lagretVedtakBuilder.medFagsakId(FAGSAK_ID).medBehandlingId(BEHANDLING_ID);
        try {
            lagretVedtakBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("xmlClob");
        }

        // mangler vedtakType
        lagretVedtakBuilder.medXmlClob(STRING_XML);
        try {
            lagretVedtakBuilder.build();
            fail(FORVENTET_EXCEPTION);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("lagretVedtakType");
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        lagretVedtak = lagBuilderMedPaakrevdeFelter().build();

        assertThat(lagretVedtak).isNotNull();
        assertThat(lagretVedtak).isNotEqualTo("blabla");
        assertThat(lagretVedtak).isEqualTo(lagretVedtak);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        lagretVedtakBuilder = lagBuilderMedPaakrevdeFelter();
        lagretVedtak = lagretVedtakBuilder.build();
        lagretVedtak2 = lagretVedtakBuilder.build();

        assertThat(lagretVedtak).isEqualTo(lagretVedtak2);
        assertThat(lagretVedtak2).isEqualTo(lagretVedtak);

        lagretVedtak2 = lagretVedtakBuilder.medFagsakId(252L).build();
        assertThat(lagretVedtak).isNotEqualTo(lagretVedtak2);
        assertThat(lagretVedtak2).isNotEqualTo(lagretVedtak);
    }

    @Test
    public void skal_bruke_fagsakId_i_equalsOgHashCode() {
        lagretVedtakBuilder = lagBuilderMedPaakrevdeFelter();
        lagretVedtak = lagretVedtakBuilder.build();
        lagretVedtakBuilder.medFagsakId(302L);
        lagretVedtak2 = lagretVedtakBuilder.build();

        assertThat(lagretVedtak).isNotEqualTo(lagretVedtak2);
        assertThat(lagretVedtak.hashCode()).isNotEqualTo(lagretVedtak2.hashCode());
    }

    @Test
    public void skal_bruke_behandlingId_i_equalsOgHashCode() {
        lagretVedtakBuilder = lagBuilderMedPaakrevdeFelter();
        lagretVedtak = lagretVedtakBuilder.build();
        lagretVedtakBuilder.medBehandlingId(525L);
        lagretVedtak2 = lagretVedtakBuilder.build();

        assertThat(lagretVedtak).isNotEqualTo(lagretVedtak2);
        assertThat(lagretVedtak.hashCode()).isNotEqualTo(lagretVedtak2.hashCode());

    }

    @Test
    public void skal_bruke_dokument_i_equalsOgHashCode() {
        lagretVedtakBuilder = lagBuilderMedPaakrevdeFelter();
        lagretVedtak = lagretVedtakBuilder.build();
        lagretVedtakBuilder.medXmlClob("<?xml version=\"1.0\" encoding=\"UTF-8\"?><element>XML string</element>");
        lagretVedtak2 = lagretVedtakBuilder.build();

        assertThat(lagretVedtak).isNotEqualTo(lagretVedtak2);
        assertThat(lagretVedtak.hashCode()).isNotEqualTo(lagretVedtak2.hashCode());
    }

    @Test
    public void skal_bruke_vedtakType_i_equalsOgHashCode() {
        lagretVedtakBuilder = lagBuilderMedPaakrevdeFelter();
        lagretVedtak = lagretVedtakBuilder.build();
        lagretVedtakBuilder.medVedtakType(LagretVedtakType.FODSEL);
        lagretVedtak2 = lagretVedtakBuilder.build();

        assertThat(lagretVedtak).isNotEqualTo(lagretVedtak2);
        assertThat(lagretVedtak.hashCode()).isNotEqualTo(lagretVedtak2.hashCode());
    }

    // ----------------------------------------------------------

    private LagretVedtak.Builder lagBuilderMedPaakrevdeFelter() {
        return LagretVedtak.builder()
                .medFagsakId(FAGSAK_ID)
                .medBehandlingId(BEHANDLING_ID)
                .medXmlClob(STRING_XML)
                .medVedtakType(LAGRET_VEDTAK_TYPE);
    }

}
