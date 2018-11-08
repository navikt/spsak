package no.nav.foreldrepenger.domene.familiehendelse.omsorg.impl;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

class OmsorgsvilkårAksjonspunkt {

    private AksjonspunktRepository aksjonspunktRepository;

    OmsorgsvilkårAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    void oppdater(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {

        // Rydd opp gjenopprettede aksjonspunkt på andre omsorgsvilkår ved eventuelt tilbakehopp
        behandling.getAksjonspunkter().stream()
            .filter(ap -> OmsorgsvilkårKonfigurasjon.getOmsorgsovertakelseAksjonspunkter().contains(ap.getAksjonspunktDefinisjon()))
            .filter(ap -> !Objects.equals(ap.getAksjonspunktDefinisjon(), aksjonspunktDefinisjon)) // ikke sett seg selv til avbrutt
            .filter(Aksjonspunkt::erOpprettet)
            .forEach(ap -> aksjonspunktRepository.setTilAvbrutt(ap));
    }
}
