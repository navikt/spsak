package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.KøKontroller;

@ApplicationScoped
public class BerørtBehandlingKontrollerImpl implements BerørtBehandlingKontroller {

    private BehandlingRevurderingRepository behandlingRevurderingRepository;
    private BehandlingRepository behandlingRepository;
    private Behandlingsoppretter behandlingsoppretter;
    private KøKontroller køKontroller;

    public BerørtBehandlingKontrollerImpl() {
        // NOSONAR
    }

    @Inject
    public BerørtBehandlingKontrollerImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                          Behandlingsoppretter behandlingsoppretter,
                                          KøKontroller køKontroller) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.behandlingRevurderingRepository = behandlingRepositoryProvider.getBehandlingRevurderingRepository();
        this.behandlingsoppretter = behandlingsoppretter;
        this.køKontroller = køKontroller;
    }

    @Override
    public void vurderNesteOppgaveIBehandlingskø(Long behandlingId) {
        Fagsak fagsakBruker = behandlingRepository.hentBehandling(behandlingId).getFagsak();
        Optional<Fagsak> fagsakPåMedforelder = finnFagsakPåMedforelder(fagsakBruker);
        if (fagsakPåMedforelder.isPresent()) {
            håndterKøForMedforelder(fagsakPåMedforelder.get(), behandlingId);
        } else {
            håndterKøForBruker(fagsakBruker);
        }
    }

    private void håndterKøForMedforelder(Fagsak fagsakMedforelder, Long behandlingIdBruker) {
        Optional<Behandling> innvilgetYtelsesbehandlingMedforelder = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(fagsakMedforelder.getId());
        Behandling iverksattBehandlingBruker = behandlingRepository.hentBehandling(behandlingIdBruker);

        if (innvilgetYtelsesbehandlingMedforelder.isPresent() && !iverksattBehandlingBruker.getBehandlingsresultat().getBehandlingResultatType().erHenlagt()
            && !behandlingErKunBerørt(iverksattBehandlingBruker)) {
            // Iverksatt behandling har flere og/eller andre årsaker enn berørt -> opprett NY berørt beh. på medforelder
            opprettBerørtBehandling(fagsakMedforelder);
            return;
        }
        håndterKø(fagsakMedforelder);
    }

    private boolean behandlingErKunBerørt(Behandling iverksattBehandling) {
        return iverksattBehandling.getBehandlingÅrsaker().size() == 1
            && iverksattBehandling.getBehandlingÅrsaker().stream()
            .anyMatch(årsak -> BehandlingÅrsakType.BERØRT_BEHANDLING.equals(årsak.getBehandlingÅrsakType()));
    }

    private void håndterKø(Fagsak fagsak) {
        Optional<Behandling> køetBehandling = finnKøetBehandling(fagsak);
        if (køetBehandling.isPresent()) {
            dekøBehandling(køetBehandling.get());
        } else {
            // Ta fra kø til annen forelder når egen kø er tom
            Optional<Behandling> køetBehandlingMedforelder = finnKøetBehandlingMedforelder(fagsak);
            køetBehandlingMedforelder.ifPresent(this::dekøBehandling);
        }
    }

    private void håndterKøForBruker(Fagsak fagsak) {
        Optional<Behandling> køetBehandling = finnKøetBehandling(fagsak);
        køetBehandling.ifPresent(this::dekøBehandling);
    }

    private void opprettBerørtBehandling(Fagsak fagsakMedforelder) {
        behandlingsoppretter.opprettBerørtBehandling(fagsakMedforelder); // Oppretter også startBehandling task.
    }

    private void dekøBehandling(Behandling behandling) {
        køKontroller.dekøNesteBehandlingISakskompleks(behandling);
    }

    private Optional<Behandling> finnKøetBehandling(Fagsak fagsak) {
        return behandlingRevurderingRepository.finnKøetYtelsesbehandling(fagsak.getId());
    }

    private Optional<Behandling> finnKøetBehandlingMedforelder(Fagsak fagsak) {
        return behandlingRevurderingRepository.finnKøetBehandlingMedforelder(fagsak);
    }

    private Optional<Fagsak> finnFagsakPåMedforelder(Fagsak fagsak) {
        return behandlingRevurderingRepository.finnFagsakPåMedforelder(fagsak);
    }
}
