package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BehandlingskontrollRepositoryImpl implements BehandlingskontrollRepository {

    @Deprecated
    private InternalManipulerBehandlingImpl manipulator;
    @Deprecated
    private BehandlingRepository behandlingRepository;
    @Deprecated
    private BehandlingLåsRepository behandlingLåsRepository;

    private EntityManager entityManager;

    BehandlingskontrollRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingskontrollRepositoryImpl(BehandlingRepository behandlingRepository
                                             , BehandlingLåsRepository behandlingLåsRepository
                                             , KodeverkRepository kodeverkRepository
                                             , @VLPersistenceUnit EntityManager entityManager) {
        this.behandlingRepository = Objects.requireNonNull(behandlingRepository, "behandlingRepository");
        this.behandlingLåsRepository = Objects.requireNonNull(behandlingLåsRepository, "behandlingLåsRepository");
        this.manipulator = new InternalManipulerBehandlingImpl(behandlingRepository, kodeverkRepository);

        this.entityManager = Objects.requireNonNull(entityManager, "entityManager");
    }

    @Override
    public void avsluttBehandling(Long behandlingId) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        behandling.avsluttBehandling();

        behandlingRepository.lagre(behandling, behandlingLåsRepository.taLås(behandlingId));
    }

    @Override
    public void nesteBehandlingStegStatusVedUtført(Long behandlingId, BehandlingStegTilstand nyttSteg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void nesteBehandlingStegStatusVedTilbakeføring(Long behandlingId, BehandlingStegTilstand nyttSteg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void nesteBehandlingStegStatusVedFremføring(Long behandlingId, BehandlingStegTilstand nyttSteg) {
        // TODO Auto-generated method stub

    }

    @Override
    public Optional<BehandlingStegTilstand> getAktivtBehandlingStegTilstand(Long behandlingId) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        return behandling.getBehandlingStegTilstand();
    }

    @Override
    public BehandlingStegTilstand getAktivtBehandlingStegTilstandDefinitiv(Long behandlingId) {
        return getAktivtBehandlingStegTilstand(behandlingId)
            .orElseThrow(() -> new IllegalStateException("Utvikler-feil: har ikke aktivt behandling steg for behanldingId:" + behandlingId));
    }

    @Override
    public BehandlingStegTilstand getAktivtBehandlingStegTilstandDefinitiv(Long behandlingId, BehandlingStegType stegType) {
        return getAktivtBehandlingStegTilstand(behandlingId).orElseThrow(() -> new IllegalStateException(
            "Utvikler-feil: Kan ikke ha flere steg samtidig åpne for stegType[" + stegType + "], behandlingId" + behandlingId)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public Optional<BehandlingStegTilstand> getAktivtBehandlingStegTilstand(Long behandlingId, BehandlingStegType stegType) {
        var result = getAktivtBehandlingStegTilstand(behandlingId).filter(t -> Objects.equals(stegType, t.getStegType()));
        return result;
    }

    @Override
    public List<BehandlingStegTilstand> getBehandlingStegTilstandHistorikk(Long behandlingId) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        return behandling.getBehandlingStegTilstandHistorikk().collect(Collectors.toList());
    }

}
