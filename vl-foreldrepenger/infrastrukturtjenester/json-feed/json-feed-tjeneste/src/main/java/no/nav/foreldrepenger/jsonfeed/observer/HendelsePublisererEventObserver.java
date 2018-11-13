package no.nav.foreldrepenger.jsonfeed.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEvent;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEvent;
import no.nav.foreldrepenger.jsonfeed.HendelsePublisererTjeneste;

@ApplicationScoped
public class HendelsePublisererEventObserver {

    private HendelsePublisererTjeneste hendelsePublisererTjeneste;

    HendelsePublisererEventObserver() {
        //Classic Design Institute
    }

    @Inject
    public HendelsePublisererEventObserver(HendelsePublisererTjeneste hendelsePublisererTjeneste) {
        this.hendelsePublisererTjeneste = hendelsePublisererTjeneste;
    }

    public void observerBehandlingVedtak(@Observes BehandlingVedtakEvent event) {

        hendelsePublisererTjeneste.lagreVedtak(event.getVedtak());
    }

    public void observerFagsakAvsluttetEvent(@Observes FagsakStatusEvent event) {
        if (FagsakStatus.LØPENDE.equals(event.getForrigeStatus()) && FagsakStatus.AVSLUTTET.equals(event.getNyStatus())) {
            hendelsePublisererTjeneste.lagreFagsakAvsluttet(event);
        }
    }
}
