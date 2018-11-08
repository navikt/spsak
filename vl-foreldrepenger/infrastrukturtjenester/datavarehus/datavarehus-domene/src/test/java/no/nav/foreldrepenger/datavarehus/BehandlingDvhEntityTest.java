package no.nav.foreldrepenger.datavarehus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BehandlingDvhEntityTest {

    @Test
    public void skal_bygge_instans_av_behandlingDvh() {
        BehandlingDvh behandlingDvh = DatavarehusTestUtils.byggBehandlingDvh();

        assertThat(behandlingDvh.getAnsvarligBeslutter()).isEqualTo(DatavarehusTestUtils.ANSVARLIG_BESLUTTER);
        assertThat(behandlingDvh.getAnsvarligSaksbehandler()).isEqualTo(DatavarehusTestUtils.ANSVARLIG_SAKSBEHANDLER);
        assertThat(behandlingDvh.getBehandlendeEnhet()).isEqualTo(DatavarehusTestUtils.BEHANDLENDE_ENHET);
        assertThat(behandlingDvh.getBehandlingId()).isEqualTo(DatavarehusTestUtils.BEHANDLING_ID);
        assertThat(behandlingDvh.getBehandlingResultatType()).isEqualTo(DatavarehusTestUtils.BEHANDLING_RESULTAT_TYPE);
        assertThat(behandlingDvh.getBehandlingStatus()).isEqualTo(DatavarehusTestUtils.BEHANDLING_STATUS);
        assertThat(behandlingDvh.getBehandlingType()).isEqualTo(DatavarehusTestUtils.BEHANDLING_TYPE);
        assertThat(behandlingDvh.getEndretAv()).isEqualTo(DatavarehusTestUtils.ENDRET_AV);
        assertThat(behandlingDvh.getFagsakId()).isEqualTo(DatavarehusTestUtils.FAGSAK_ID);
        assertThat(behandlingDvh.getFunksjonellTid()).isEqualTo(DatavarehusTestUtils.FUNKSJONELL_TID);
        assertThat(behandlingDvh.getOpprettetDato()).isEqualTo(DatavarehusTestUtils.OPPRETTET_DATE);
        assertThat(behandlingDvh.getUtlandstilsnitt()).isEqualTo(DatavarehusTestUtils.UTLANDSTILSNITT);
        assertThat(behandlingDvh.getVedtakId()).isEqualTo(DatavarehusTestUtils.VEDTAK_ID);
        assertThat(behandlingDvh.isToTrinnsBehandling()).isEqualTo(true);
    }
}
