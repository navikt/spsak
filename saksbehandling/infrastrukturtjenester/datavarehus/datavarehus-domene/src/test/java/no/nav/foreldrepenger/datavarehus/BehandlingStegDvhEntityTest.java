package no.nav.foreldrepenger.datavarehus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BehandlingStegDvhEntityTest {

    @Test
    public void skal_bygge_instans_av_behandlingDvh() {
        BehandlingStegDvh behandlingStegDvh = DatavarehusTestUtils.byggBehandlingStegDvh();


        assertThat(behandlingStegDvh.getBehandlingId()).isEqualTo(DatavarehusTestUtils.BEHANDLING_ID);
        assertThat(behandlingStegDvh.getBehandlingStegId()).isEqualTo(DatavarehusTestUtils.BEHANDLING_STEG_ID);
        assertThat(behandlingStegDvh.getBehandlingStegStatus()).isEqualTo(DatavarehusTestUtils.BEHANDLING_STEG_STATUS);
        assertThat(behandlingStegDvh.getBehandlingStegType()).isEqualTo(DatavarehusTestUtils.BEHANDLING_STEG_TYPE);
        assertThat(behandlingStegDvh.getEndretAv()).isEqualTo(DatavarehusTestUtils.ENDRET_AV);
        assertThat(behandlingStegDvh.getFunksjonellTid()).isEqualTo(DatavarehusTestUtils.FUNKSJONELL_TID);
    }



}
