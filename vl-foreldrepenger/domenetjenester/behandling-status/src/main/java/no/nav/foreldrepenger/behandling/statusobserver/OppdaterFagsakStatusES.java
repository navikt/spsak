package no.nav.foreldrepenger.behandling.statusobserver;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;

@ApplicationScoped
@FagsakYtelseTypeRef("ES")
public class OppdaterFagsakStatusES implements OppdaterFagsakStatus {

    private BehandlingRepository behandlingRepository;
    private OppdaterFagsakStatusFelles oppdaterFagsakStatusFelles;

    OppdaterFagsakStatusES() {
        // CDI
    }

    @Inject
    public OppdaterFagsakStatusES(BehandlingRepository behandlingRepository,
                                  OppdaterFagsakStatusFelles oppdaterFagsakStatusFelles) {
        this.behandlingRepository = behandlingRepository;
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

        if (!åpneBortsettFraAngitt.isPresent()) {
            // ingen andre behandlinger er åpne
            oppdaterFagsakStatusFelles.oppdaterFagsakStatus(behandling, FagsakStatus.AVSLUTTET);
        }
    }
}
