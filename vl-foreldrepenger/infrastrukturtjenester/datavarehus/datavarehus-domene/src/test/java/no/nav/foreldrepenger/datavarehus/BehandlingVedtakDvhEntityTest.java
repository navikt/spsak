package no.nav.foreldrepenger.datavarehus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BehandlingVedtakDvhEntityTest {

    @Test
    public void skal_bygge_instans_av_behandlingVedtakDvh() {
        BehandlingVedtakDvh behandlingVedtakDvh = DatavarehusTestUtils.byggBehandlingVedtakDvh();

        assertThat(behandlingVedtakDvh.getAnsvarligBeslutter()).isEqualTo(DatavarehusTestUtils.ANSVARLIG_BESLUTTER);
        assertThat(behandlingVedtakDvh.getAnsvarligSaksbehandler()).isEqualTo(DatavarehusTestUtils.ANSVARLIG_SAKSBEHANDLER);
        assertThat(behandlingVedtakDvh.getBehandlingId()).isEqualTo(DatavarehusTestUtils.BEHANDLING_ID);
        assertThat(behandlingVedtakDvh.getEndretAv()).isEqualTo(DatavarehusTestUtils.ENDRET_AV);
        assertThat(behandlingVedtakDvh.getFunksjonellTid()).isEqualTo(DatavarehusTestUtils.FUNKSJONELL_TID);
        assertThat(behandlingVedtakDvh.getGodkjennendeEnhet()).isEqualTo(DatavarehusTestUtils.GODKJENNENDE_ENHET);
        assertThat(behandlingVedtakDvh.getIverksettingStatus()).isEqualTo(DatavarehusTestUtils.IVERKSETTING_STATUS);
        assertThat(behandlingVedtakDvh.getOpprettetDato()).isEqualTo(DatavarehusTestUtils.OPPRETTET_DATE);
        assertThat(behandlingVedtakDvh.getVedtakDato()).isEqualTo(DatavarehusTestUtils.VEDTAK_DATO);
        assertThat(behandlingVedtakDvh.getVedtakId()).isEqualTo(DatavarehusTestUtils.VEDTAK_ID);
        assertThat(behandlingVedtakDvh.getVedtakResultatTypeKode()).isEqualTo(DatavarehusTestUtils.VEDTAK_RESULTAT_TYPE);

    }
}
