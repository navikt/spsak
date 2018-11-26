package no.nav.foreldrepenger.datavarehus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class FagsakDvhEntityTest {

    @Test
    public void skal_bygge_instans_av_fagsakDvh() {
        FagsakDvh fagsakDvh = DatavarehusTestUtils.byggFagsakDvhForTest();

        assertThat(fagsakDvh.getBrukerId()).isEqualTo(DatavarehusTestUtils.BRUKER_ID);
        assertThat(fagsakDvh.getBrukerAktørId()).isEqualTo(DatavarehusTestUtils.BRUKER_AKTØR_ID);
        assertThat(fagsakDvh.getEndretAv()).isEqualTo(DatavarehusTestUtils.ENDRET_AV);
        assertThat(fagsakDvh.getFagsakId()).isEqualTo(DatavarehusTestUtils.FAGSAK_ID);
        assertThat(fagsakDvh.getFagsakStatus()).isEqualTo(DatavarehusTestUtils.FAGSAK_STATUS);
        assertThat(fagsakDvh.getFagsakYtelse()).isEqualTo(DatavarehusTestUtils.FAGSAK_YTELSE);
        assertThat(fagsakDvh.getFunksjonellTid()).isEqualTo(DatavarehusTestUtils.FUNKSJONELL_TID);
        assertThat(fagsakDvh.getOpprettetDato()).isEqualTo(DatavarehusTestUtils.OPPRETTET_DATE);
        assertThat(fagsakDvh.getSaksnummer()).isEqualTo(DatavarehusTestUtils.SAKSNUMMER);
    }
}
