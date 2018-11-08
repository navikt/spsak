package no.nav.foreldrepenger.behandling.revurdering.testutil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;

@ApplicationScoped
public class BeregningRevurderingTestUtil {

    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;

    BeregningRevurderingTestUtil() {
        // for CDI
    }

    @Inject
    public BeregningRevurderingTestUtil(BehandlingRepositoryProvider repositoryProvider) {
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        fagsakRepository = repositoryProvider.getFagsakRepository();
    }

    public void avsluttBehandling(Behandling behandling) {
        if (behandling == null) {
            throw new IllegalStateException("Du må definere en behandling før du kan avslutten den");
        }
        avsluttBehandlingOgFagsak(behandling);
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
    }
}
