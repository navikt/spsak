package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
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
    
    public BehandlingskontrollRepositoryImpl(GrunnlagRepositoryProvider repositoryProvider, EntityManager entityManager) {
        this(repositoryProvider.getBehandlingRepository(), repositoryProvider.getBehandlingLåsRepository(), repositoryProvider.getKodeverkRepository(), entityManager);
    }

    @Override
    public void avsluttBehandling(Long behandlingId) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingLåsRepository.taLås(behandlingId));
    }

    @Override
    public void nesteBehandlingStegStatusVedUtført(Long behandlingId, StegTilstand nyttSteg) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        manipulator.forceOppdaterBehandlingSteg(behandling, nyttSteg.getStegType(), nyttSteg.getStatus(), BehandlingStegStatus.UTFØRT);
        behandlingRepository.lagre(behandling, behandlingLåsRepository.taLås(behandlingId));
    }

    @Override
    public void nesteBehandlingStegStatusVedTilbakeføring(Long behandlingId, StegTilstand nyttSteg) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        manipulator.forceOppdaterBehandlingSteg(behandling, nyttSteg.getStegType(), nyttSteg.getStatus(), BehandlingStegStatus.TILBAKEFØRT);
        behandlingRepository.lagre(behandling, behandlingLåsRepository.taLås(behandlingId));
    }

    @Override
    public void nesteBehandlingStegStatusVedFremføring(Long behandlingId, StegTilstand nyttSteg) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        manipulator.forceOppdaterBehandlingSteg(behandling, nyttSteg.getStegType(), nyttSteg.getStatus(), BehandlingStegStatus.FREMOVERFØRT);
        behandlingRepository.lagre(behandling, behandlingLåsRepository.taLås(behandlingId));
    }
    
    @Override
    public void nesteBehandlingStegStatusIntern(Long behandlingId, BehandlingStegType stegType, BehandlingStegStatus nyStegStatus) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        manipulator.forceOppdaterBehandlingSteg(behandling, stegType, nyStegStatus);
        behandlingRepository.lagre(behandling, behandlingLåsRepository.taLås(behandlingId));
    }
   
    
    @Override
    public BehandlingskontrollTilstand getBehandlingskontrollTilstand(Long behandlingId) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        
        BehandlingskontrollTilstand tilstand = new BehandlingskontrollTilstand(behandlingId, behandling.getFagsakYtelseType(), behandling.getType());
        Map<BehandlingÅrsakType, Boolean> årsaker = new LinkedHashMap<>();
        behandling.getBehandlingÅrsaker().forEach(å -> årsaker.put(å.getBehandlingÅrsakType(), å.erManueltOpprettet()));
        
        tilstand.setBehandlingÅrsaker(årsaker);
        tilstand.setStatus(behandling.getStatus());
        tilstand.setStartpunkt(behandling.getStartpunkt());
        
        // tilstand.setAksjonspunkter(aksjonspunkter); // TODO
        
        Optional<StegTilstand> stegTilstand = StegTilstand.fra(behandling.getBehandlingStegTilstand());
        tilstand.setStegTilstand(stegTilstand);
        
        return tilstand;
    }

    @Deprecated(forRemoval = true)
    @Override
    public Optional<BehandlingStegTilstand> getAktivtBehandlingStegTilstand(Long behandlingId) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        return behandling.getBehandlingStegTilstand();
    }

    @Override
    public List<BehandlingStegTilstand> getBehandlingStegTilstandHistorikk(Long behandlingId) {
        Behandling behandling = entityManager.find(Behandling.class, behandlingId);
        return behandling.getBehandlingStegTilstandHistorikk().collect(Collectors.toList());
    }

}
