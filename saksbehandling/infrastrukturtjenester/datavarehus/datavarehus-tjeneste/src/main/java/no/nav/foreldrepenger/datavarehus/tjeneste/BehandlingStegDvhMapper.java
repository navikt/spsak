package no.nav.foreldrepenger.datavarehus.tjeneste;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.datavarehus.BehandlingStegDvh;
import no.nav.vedtak.util.FPDateUtil;

public class BehandlingStegDvhMapper {

    public BehandlingStegDvh map(BehandlingStegTilstand behandlingStegTilstand) {
        return BehandlingStegDvh.builder()
            .behandlingId(behandlingStegTilstand.getBehandlingId())
            .behandlingStegId(behandlingStegTilstand.getId())
            .behandlingStegStatus(finnBehandlingStegStatusKode(behandlingStegTilstand))
            .behandlingStegType(behandlingStegTilstand.getStegType().getKode())
            .endretAv(CommonDvhMapper.finnEndretAvEllerOpprettetAv(behandlingStegTilstand))
            .funksjonellTid(FPDateUtil.nå())
            .build();
    }

    private String finnBehandlingStegStatusKode(BehandlingStegTilstand behandlingStegTilstand) {
        return Optional.ofNullable(behandlingStegTilstand)
                .map(BehandlingStegTilstand::getStatus)
                .map(BehandlingStegStatus::getKode).orElse(null);
    }
}
