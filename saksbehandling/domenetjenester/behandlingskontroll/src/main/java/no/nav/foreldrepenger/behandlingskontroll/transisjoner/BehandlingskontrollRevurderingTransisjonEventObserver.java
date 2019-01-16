package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;

@ApplicationScoped
class BehandlingskontrollRevurderingTransisjonEventObserver {

    private AksjonspunktRepository aksjonspunktRepository;

    private BehandlingRepository behandlingRepository;

    private BehandlingModellRepository behandlingModellRepository;

    BehandlingskontrollRevurderingTransisjonEventObserver() {
        //for CDI proxy
    }

    @Inject
    public BehandlingskontrollRevurderingTransisjonEventObserver(AksjonspunktRepository aksjonspunktRepository, BehandlingRepository behandlingRepository, BehandlingModellRepository behandlingModellRepository) {
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.behandlingRepository = behandlingRepository;
        this.behandlingModellRepository = behandlingModellRepository;
    }

    public void observerBehandlingSteg(@Observes BehandlingTransisjonEvent event) {
        Behandling behandling = behandlingRepository.hentBehandling(event.getBehandlingId());
        BehandlingModellImpl behandlingModell = behandlingModellRepository.getModell(behandling.getType(), behandling.getFagsakYtelseType());
        StegTransisjon transisjon = behandlingModell.finnTransisjon(event.getTransisjonIdentifikator());

        if (transisjon instanceof RevurderingFremoverhoppTransisjon) {
            RevurderingFremoverhoppTransisjon revurderingTransisjon = (RevurderingFremoverhoppTransisjon) transisjon;
            håndterFremtidigeAksjonspunkter(behandling, behandlingModell, revurderingTransisjon);
        }
    }

    private void håndterFremtidigeAksjonspunkter(Behandling behandling, BehandlingModellImpl behandlingModell, RevurderingFremoverhoppTransisjon revurderingTransisjon) {
        List<Aksjonspunkt> fremtidigeAksjonspunkter = hentFremtidigeAksjonspunkter(behandling, behandlingModell, revurderingTransisjon);
        reaktiverManueltOpprettedeInaktiveAksjonspunkter(fremtidigeAksjonspunkter);
        markerIkkeManueltOpprettedeInaktiveAksjonspunkterSomSlettet(fremtidigeAksjonspunkter);
    }

    private List<Aksjonspunkt> hentFremtidigeAksjonspunkter(Behandling behandling, BehandlingModellImpl behandlingModell, RevurderingFremoverhoppTransisjon revurderingTransisjon) {
        BehandlingStegType målsteg = revurderingTransisjon.getMålsteg();
        Set<String> definisjoner = behandlingModell.finnAksjonspunktDefinisjonerFraOgMed(målsteg, true);
        return behandling.getAlleAksjonspunkterInklInaktive().stream()
            .filter(a -> definisjoner.contains(a.getAksjonspunktDefinisjon().getKode()))
            .collect(Collectors.toList());
    }

    private void reaktiverManueltOpprettedeInaktiveAksjonspunkter(List<Aksjonspunkt> fremtidigeAksjonspunkter) {
        fremtidigeAksjonspunkter.stream()
            .filter(a -> !a.erAktivt() && a.erManueltOpprettet())
            .forEach(a -> {
                aksjonspunktRepository.reaktiver(a);
                aksjonspunktRepository.setReåpnet(a);
            });
    }

    private void markerIkkeManueltOpprettedeInaktiveAksjonspunkterSomSlettet(List<Aksjonspunkt> fremtidigeAksjonspunkter) {
        List<Aksjonspunkt> aksjonspunkterSomSKalFjernes = fremtidigeAksjonspunkter.stream()
            .filter(a -> !a.erAktivt() && !a.erManueltOpprettet())
            .collect(Collectors.toList());

        aksjonspunkterSomSKalFjernes.forEach(ap -> {
            aksjonspunktRepository.settInaktivSomSlettet(ap);
        });
    }
}



