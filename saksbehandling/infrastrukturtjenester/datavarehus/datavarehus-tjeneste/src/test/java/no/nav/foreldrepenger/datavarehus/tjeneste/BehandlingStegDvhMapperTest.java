package no.nav.foreldrepenger.datavarehus.tjeneste;

import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLING_STEG_ID;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLING_STEG_STATUS;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.BEHANDLING_STEG_TYPE;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.OPPRETTET_AV;
import static no.nav.foreldrepenger.datavarehus.tjeneste.DvhTestDataUtil.OPPRETTET_TID;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import no.nav.vedtak.felles.testutilities.Whitebox;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.BehandlingStegDvh;

@SuppressWarnings("deprecation")
public class BehandlingStegDvhMapperTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    private BehandlingStegDvhMapper mapper = new BehandlingStegDvhMapper();

    @Test
    public void skal_mappe_til_behandling_steg_dvh() {

        BehandlingStegTilstand behandlingStegTilstand = new BehandlingStegTilstand(ScenarioMorSøkerEngangsstønad.forDefaultAktør().lagMocked(),
            BEHANDLING_STEG_TYPE, BEHANDLING_STEG_STATUS);
        Whitebox.setInternalState(behandlingStegTilstand, "id", BEHANDLING_STEG_ID);
        Whitebox.setInternalState(behandlingStegTilstand, OPPRETTET_AV, OPPRETTET_AV);
        Whitebox.setInternalState(behandlingStegTilstand, "opprettetTidspunkt", OPPRETTET_TID);
        BehandlingStegDvh dvh = mapper.map(behandlingStegTilstand);

        assertThat(dvh).isNotNull();
        assertThat(dvh.getBehandlingId()).isEqualTo(behandlingStegTilstand.getBehandling().getId());
        assertThat(dvh.getBehandlingStegId()).isEqualTo(BEHANDLING_STEG_ID);
        assertThat(dvh.getBehandlingStegStatus()).isEqualTo(BEHANDLING_STEG_STATUS.getKode());
        assertThat(dvh.getBehandlingStegType()).isEqualTo(BEHANDLING_STEG_TYPE.getKode());
        assertThat(dvh.getEndretAv()).isEqualTo(OPPRETTET_AV);
    }


}
