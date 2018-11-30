package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;

public class BehandlingskontrollDto {

    private BehandlingStatus status;
    private BehandlingStegType behandlingSteg;
    private Set<Aksjonspunkt> aksjonspunkter;

    public BehandlingskontrollDto(BehandlingStatus status, BehandlingStegType behandlingSteg, Set<Aksjonspunkt> aksjonspunkter) {
        this.status = status;
        this.behandlingSteg = behandlingSteg;
        this.aksjonspunkter = aksjonspunkter;
    }

    public BehandlingStatus getStatus() {
        return status;
    }

    public BehandlingStegType getBehandlingSteg() {
        return behandlingSteg;
    }

    public List<String> getAksjonspunkter() {
        return aksjonspunkter.stream()
            .map(a -> a.getAksjonspunktDefinisjon().getBeskrivelse())
            .collect(toList());
    }

}
