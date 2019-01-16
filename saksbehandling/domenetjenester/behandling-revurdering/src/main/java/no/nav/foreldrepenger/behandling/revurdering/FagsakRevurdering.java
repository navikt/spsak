package no.nav.foreldrepenger.behandling.revurdering;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

class FagsakRevurdering {

    private BehandlingRepository behandlingRepository;

    public FagsakRevurdering(BehandlingRepository behandlingRepository) {
        this.behandlingRepository = behandlingRepository;
    }

    Boolean kanRevurderingOpprettes(Fagsak fagsak) {
        if (harÅpenBehandling(fagsak)) {
            return false;
        }
        List<Behandling> behandlinger = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer());
        return kanRevurderingOpprettes(behandlinger);
    }

    private boolean kanRevurderingOpprettes(List<Behandling> behandlinger) {
        Optional<Behandling> gjeldendeBehandling = hentBehandlingMedVedtak(behandlinger);
        if (gjeldendeBehandling.isEmpty()) {
            return false;
        }
        return true;
    }

    private Optional<Behandling> hentBehandlingMedVedtak(List<Behandling> behandlinger) {
        List<Behandling> behandlingerMedVedtak = behandlinger.stream()
            .filter(behandling -> asList(BehandlingStatus.AVSLUTTET, BehandlingStatus.IVERKSETTER_VEDTAK).contains(behandling.getStatus()))
            .filter(behandling -> !behandlingRepository.hentResultat(behandling.getId()).isBehandlingHenlagt())
            .collect(Collectors.toList());
        List<Behandling> sorterteBehandlinger = behandlingerMedVedtak.stream().sorted(new BehandlingAvsluttetDatoComparator()).collect(Collectors.toList());
        return sorterteBehandlinger.isEmpty() ? Optional.empty() : Optional.of(sorterteBehandlinger.get(0));
    }

    private boolean harÅpenBehandling(Fagsak fagsak) {
        List<Behandling> åpneBehandlingerForFagsakId = behandlingRepository.hentÅpneBehandlingerForFagsakId(fagsak.getId());
        return !åpneBehandlingerForFagsakId.isEmpty();
    }

    static class BehandlingAvsluttetDatoComparator implements Comparator<Behandling>, Serializable {
        @Override
        public int compare(Behandling behandling, Behandling otherBehandling) {
            return otherBehandling.getAvsluttetDato() != null && behandling.getAvsluttetDato() != null ?
                otherBehandling.getAvsluttetDato().compareTo(behandling.getAvsluttetDato())
                : otherBehandling.getOpprettetDato().compareTo(behandling.getOpprettetDato());
        }
    }
}
