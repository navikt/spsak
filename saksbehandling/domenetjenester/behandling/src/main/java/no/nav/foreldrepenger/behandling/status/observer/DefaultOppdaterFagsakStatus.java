package no.nav.foreldrepenger.behandling.status.observer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;

@ApplicationScoped
public class DefaultOppdaterFagsakStatus implements OppdaterFagsakStatus {

    private BehandlingRepository behandlingRepository;
    private OppdaterFagsakStatusFelles oppdaterFagsakStatusFelles;

    DefaultOppdaterFagsakStatus() {
        // CDI
    }

    @Inject
    public DefaultOppdaterFagsakStatus(BehandlingRepositoryProvider repositoryProvider,
                                  OppdaterFagsakStatusFelles oppdaterFagsakStatusFelles) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.oppdaterFagsakStatusFelles = oppdaterFagsakStatusFelles;
    }

    @Override
    public void oppdaterFagsakNårBehandlingEndret(Behandling behandling) {
        oppdaterFagsak(behandling);
    }

    private void oppdaterFagsak(Behandling behandling) {

        if (Objects.equals(BehandlingStatus.AVSLUTTET, behandling.getStatus())) {
            avsluttFagsakNårAlleBehandlingerErLukket(behandling);
        } else {
            // hvis en Behandling har noe annen status, setter Fagsak til Under behandling
            oppdaterFagsakStatusFelles.oppdaterFagsakStatus(behandling, FagsakStatus.UNDER_BEHANDLING);
        }
    }

    private void avsluttFagsakNårAlleBehandlingerErLukket(Behandling behandling) {
        Long fagsakId = behandling.getFagsakId();
        List<Behandling> alleÅpneBehandlinger = behandlingRepository.hentBehandlingerSomIkkeErAvsluttetForFagsakId(fagsakId);

        Optional<Behandling> åpneBortsettFraAngitt = alleÅpneBehandlinger.stream()
            .filter(b -> !Objects.equals(behandling.getId(), b.getId()))
            .findAny();

        // ingen andre behandlinger er åpne
        if (!åpneBortsettFraAngitt.isPresent()) {
            if (oppdaterFagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling)) {
                oppdaterFagsakStatusFelles.oppdaterFagsakStatus(behandling, FagsakStatus.AVSLUTTET);
            } else {
                oppdaterFagsakStatusFelles.oppdaterFagsakStatus(behandling, FagsakStatus.LØPENDE);
            }
        }
    }
}
