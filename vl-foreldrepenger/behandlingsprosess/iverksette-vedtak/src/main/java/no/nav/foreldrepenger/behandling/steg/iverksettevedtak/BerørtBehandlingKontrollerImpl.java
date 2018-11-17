package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.KøKontroller;

@ApplicationScoped
public class BerørtBehandlingKontrollerImpl implements BerørtBehandlingKontroller {

    private BehandlingRevurderingRepository behandlingRevurderingRepository;
    private BehandlingRepository behandlingRepository;
    private KøKontroller køKontroller;

    public BerørtBehandlingKontrollerImpl() {
        // NOSONAR
    }

    @Inject
    public BerørtBehandlingKontrollerImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                          KøKontroller køKontroller) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.behandlingRevurderingRepository = behandlingRepositoryProvider.getBehandlingRevurderingRepository();
        this.køKontroller = køKontroller;
    }

    @Override
    public void vurderNesteOppgaveIBehandlingskø(Long behandlingId) {
        Fagsak fagsakBruker = behandlingRepository.hentBehandling(behandlingId).getFagsak();
        håndterKøForBruker(fagsakBruker);
    }

    private void håndterKøForBruker(Fagsak fagsak) {
        Optional<Behandling> køetBehandling = finnKøetBehandling(fagsak);
        køetBehandling.ifPresent(this::dekøBehandling);
    }

    private void dekøBehandling(Behandling behandling) {
        køKontroller.dekøNesteBehandlingISakskompleks(behandling);
    }

    private Optional<Behandling> finnKøetBehandling(Fagsak fagsak) {
        return behandlingRevurderingRepository.finnKøetYtelsesbehandling(fagsak.getId());
    }
}
