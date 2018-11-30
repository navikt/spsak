package no.nav.foreldrepenger.datavarehus.tjeneste;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.datavarehus.AksjonspunktDvh;
import no.nav.vedtak.util.FPDateUtil;

public class AksjonspunktDvhMapper {

    public AksjonspunktDvh map(Aksjonspunkt aksjonspunkt, Behandling behandling, Optional<BehandlingStegTilstand> behandlingStegTilstand, boolean aksjonspunktGodkjennt) {
        return AksjonspunktDvh.builder()
            .aksjonspunktDef(aksjonspunkt.getAksjonspunktDefinisjon().getKode())
            .aksjonspunktId(aksjonspunkt.getId())
            .aksjonspunktStatus(aksjonspunkt.getStatus().getKode())
            .ansvarligBeslutter(behandling.getAnsvarligBeslutter())
            .ansvarligSaksbehandler(behandling.getAnsvarligSaksbehandler())
            .behandlendeEnhetKode(behandling.getBehandlendeEnhet())
            .behandlingId(behandling.getId())
            .behandlingStegId(behandlingStegTilstand.map(BehandlingStegTilstand::getId).orElse(null))
            .endretAv(CommonDvhMapper.finnEndretAvEllerOpprettetAv(aksjonspunkt))
            .funksjonellTid(FPDateUtil.nå())
            .toTrinnsBehandling(aksjonspunkt.isToTrinnsBehandling())
            .toTrinnsBehandlingGodkjent(aksjonspunktGodkjennt)
            .build();
    }

}
