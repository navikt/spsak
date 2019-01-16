package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTransisjonEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;

@ApplicationScoped
class BehandlingskontrollHenleggelseTransisjonEventObserver {

    private BehandlingRepository behandlingRepository;

    private BehandlingModellRepository behandlingModellRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    BehandlingskontrollHenleggelseTransisjonEventObserver() {
        //for CDI proxy
    }

    @Inject
    public BehandlingskontrollHenleggelseTransisjonEventObserver(BehandlingRepository behandlingRepository, BehandlingModellRepository behandlingModellRepository, BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingModellRepository = behandlingModellRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void observerBehandlingSteg(@Observes BehandlingTransisjonEvent event) {
        Behandling behandling = behandlingRepository.hentBehandling(event.getBehandlingId());
        BehandlingModell behandlingModell = behandlingModellRepository.getModell(behandling.getType(), behandling.getFagsakYtelseType());
        StegTransisjon transisjon = behandlingModell.finnTransisjon(event.getTransisjonIdentifikator());

        if (transisjon instanceof HenleggelseTransisjon) {
            behandlingskontrollTjeneste.henleggBehandlingFraSteg(event.getKontekst(), BehandlingResultatType.HENLAGT_SØKNAD_MANGLER);

            behandlingskontrollTjeneste.lagHistorikkinnslagForHenleggelse(behandling.getId(), HistorikkinnslagType.AVBRUTT_BEH, BehandlingResultatType.HENLAGT_SØKNAD_MANGLER, null, HistorikkAktør.VEDTAKSLØSNINGEN);
        }
    }
}



