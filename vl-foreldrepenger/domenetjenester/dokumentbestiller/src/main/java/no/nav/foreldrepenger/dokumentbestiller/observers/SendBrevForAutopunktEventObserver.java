package no.nav.foreldrepenger.dokumentbestiller.observers;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.dokumentbestiller.autopunkt.SendBrevForAutopunkt;

/**
 * Observerer Aksjonspunkt og sender brev ved behov
 */
@ApplicationScoped
public class SendBrevForAutopunktEventObserver {

    BehandlingRepository behandlingRepository;
    SendBrevForAutopunkt sendBrevForAutopunkt;

    public SendBrevForAutopunktEventObserver() {
        //CDI
    }

    @Inject
    public SendBrevForAutopunktEventObserver(BehandlingRepository behandlingRepository,
                                             SendBrevForAutopunkt sendBrevForAutopunkt) {
        this.behandlingRepository = behandlingRepository;
        this.sendBrevForAutopunkt = sendBrevForAutopunkt;
    }

    public void sendBrevForAutopunkt(@Observes AksjonspunkterFunnetEvent event) {
        BehandlingskontrollKontekst kontekst = event.getKontekst();
        List<Aksjonspunkt> aksjonspunkter = event.getAksjonspunkter();
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        finnAksjonspunkerMedDef(aksjonspunkter, AksjonspunktDefinisjon.VENT_PÅ_SØKNAD)
            .ifPresent(ap -> sendBrevForAutopunkt.sendBrevForSøknadIkkeMottatt(behandling));
        finnAksjonspunkerMedDef(aksjonspunkter, AksjonspunktDefinisjon.VENT_PGA_FOR_TIDLIG_SØKNAD)
            .ifPresent(ap -> sendBrevForAutopunkt.sendBrevForTidligSøknad(behandling, ap));
        finnAksjonspunkerMedDef(aksjonspunkter, AksjonspunktDefinisjon.VENT_PÅ_FØDSEL)
            .ifPresent(ap -> sendBrevForAutopunkt.sendBrevForVenterPåFødsel(behandling, ap));
    }

    private Optional<Aksjonspunkt> finnAksjonspunkerMedDef(List<Aksjonspunkt> aksjonspunkter, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return aksjonspunkter
            .stream()
            .filter(ap -> aksjonspunktDefinisjon.equals(ap.getAksjonspunktDefinisjon()))
            .findFirst();
    }

}
