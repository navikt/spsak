package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

// FIXME SP: gir denne mening lenger? Kan fjernes?
@Dependent
public class KøKontroller {


    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;

    public KøKontroller() {
        // For CDI proxy
    }

    @Inject
    public KøKontroller(BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste) {
        this.behandlingskontrollTaskTjeneste = behandlingskontrollTaskTjeneste;
    }


    void dekøFørsteBehandlingISakskompleks(Behandling behandling) {
        opprettTaskForÅStarteBehandling(behandling);
    }

    public void dekøNesteBehandlingISakskompleks(Behandling behandling) {
        opprettTaskForÅStarteBehandling(behandling);
    }

    void opprettTaskForÅStarteBehandling(Behandling behandling) {
        behandlingskontrollTaskTjeneste.opprettFortsettBehandlingTask(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId(), Optional.of(AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING));
    }

}
