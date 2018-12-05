package no.nav.foreldrepenger.behandling.revurdering.testutil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;

@ApplicationScoped
public class BeregningRevurderingTestUtil {

    private FagsakRepository fagsakRepository;
    private BehandlingskontrollRepository behandlingskontrollRepository;

    BeregningRevurderingTestUtil() {
        // for CDI
    }

    @Inject
    public BeregningRevurderingTestUtil(BehandlingRepositoryProvider repositoryProvider) {
        fagsakRepository = repositoryProvider.getFagsakRepository();
        behandlingskontrollRepository = repositoryProvider.getBehandlingskontrollRepository();
    }

    public void avsluttBehandling(Behandling behandling) {
        if (behandling == null) {
            throw new IllegalStateException("Du må definere en behandling før du kan avslutten den");
        }
        avsluttBehandlingOgFagsak(behandling);
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        behandlingskontrollRepository.avsluttBehandling(behandling.getId());
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
    }
}
