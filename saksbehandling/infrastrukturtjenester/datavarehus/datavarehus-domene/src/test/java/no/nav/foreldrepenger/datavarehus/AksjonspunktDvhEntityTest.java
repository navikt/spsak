package no.nav.foreldrepenger.datavarehus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AksjonspunktDvhEntityTest {

    @Test
    public void skal_bygge_instans_av_aksjonspunktDvh() {
        AksjonspunktDvh aksjonspunktDvh = DatavarehusTestUtils.byggAksjonspunktDvh();

        assertThat(aksjonspunktDvh.getAksjonspunktDef()).isEqualTo(DatavarehusTestUtils.AKSJONSPUNKT_DEF);
        assertThat(aksjonspunktDvh.getAksjonspunktId()).isEqualTo(DatavarehusTestUtils.AKSJONSPUNKT_ID);
        assertThat(aksjonspunktDvh.getAksjonspunktStatus()).isEqualTo(DatavarehusTestUtils.AKSJONSPUNKT_STATUS);
        assertThat(aksjonspunktDvh.getAnsvarligBeslutter()).isEqualTo(DatavarehusTestUtils.ANSVARLIG_BESLUTTER);
        assertThat(aksjonspunktDvh.getAnsvarligSaksbehandler()).isEqualTo(DatavarehusTestUtils.ANSVARLIG_SAKSBEHANDLER);
        assertThat(aksjonspunktDvh.getBehandlingId()).isEqualTo(DatavarehusTestUtils.BEHANDLING_ID);
        assertThat(aksjonspunktDvh.getBehandlendeEnhetKode()).isEqualTo(DatavarehusTestUtils.BEHANDLENDE_ENHET);
        assertThat(aksjonspunktDvh.getBehandlingStegId()).isEqualTo(DatavarehusTestUtils.BEHANDLING_STEG_ID);
        assertThat(aksjonspunktDvh.getEndretAv()).isEqualTo(DatavarehusTestUtils.ENDRET_AV);
        assertThat(aksjonspunktDvh.getFunksjonellTid()).isEqualTo(DatavarehusTestUtils.FUNKSJONELL_TID);
        assertThat(aksjonspunktDvh.isToTrinnsBehandling()).isEqualTo(true);
        assertThat(aksjonspunktDvh.getToTrinnsBehandlingGodkjent()).isEqualTo(true);
    }
}
