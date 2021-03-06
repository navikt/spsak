package no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BehandlingVedtakRepositoryImpl implements BehandlingVedtakRepository {

    private static final String BEHANDLINGSRESULTAT_ID = "behandlingsresultatId"; //$NON-NLS-1$

    private EntityManager entityManager;

    private BehandlingRepository behandlingRepository;

    public BehandlingVedtakRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingVedtakRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, BehandlingRepository behandlingRepository) {
        this.entityManager = entityManager;
        this.behandlingRepository = behandlingRepository;
    }

    public BehandlingVedtakRepositoryImpl(EntityManager entityManager) {
        // for test
        this.entityManager = entityManager;
        if (entityManager != null) {
            this.behandlingRepository = new BehandlingRepositoryImpl(entityManager);
        }
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Optional<BehandlingVedtak> hentVedtakFor(Long behandlingsresultatId) {
        Objects.requireNonNull(behandlingsresultatId, "behandlingsresultatId"); // NOSONAR //$NON-NLS-1$
        TypedQuery<BehandlingVedtak> query = opprettVedtakQuery(behandlingsresultatId);
        return optionalFirstVedtak(query.getResultList());
    }

    @Override
    public BehandlingVedtak hentVedtakFraRevurderingensOriginaleBehandling(Behandling behandling) {
        if (!behandling.erRevurdering()) {
            throw new IllegalStateException("Utviklerfeil: Metoden skal bare kalles for revurderinger");
        }
        Behandlingsresultat originaltResultat = behandling.getOriginalBehandling().map(org -> behandlingRepository.hentResultat(org.getId()))
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Original behandling mangler på revurdering - skal ikke skje"));
        return hentVedtakFor(originaltResultat.getId())
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Original behandling har ikke behandlingsvedtak - skal ikke skje"));
    }

    @Override
    public Long lagre(BehandlingVedtak vedtak, BehandlingLås lås) {
        getEntityManager().persist(vedtak);
        verifiserBehandlingLås(lås);
        getEntityManager().flush();
        getEntityManager().refresh(vedtak.getBehandlingsresultat());
        return vedtak.getId();
    }

    // sjekk lås og oppgrader til skriv
    protected void verifiserBehandlingLås(BehandlingLås lås) {
        BehandlingLåsRepositoryImpl låsHåndterer = new BehandlingLåsRepositoryImpl(getEntityManager());
        låsHåndterer.oppdaterLåsVersjon(lås);
    }

    private TypedQuery<BehandlingVedtak> opprettVedtakQuery(Long behandlingsresultatId) {
        Objects.requireNonNull(behandlingsresultatId, BEHANDLINGSRESULTAT_ID); // $NON-NLS-1$
        TypedQuery<BehandlingVedtak> query = getEntityManager()
                .createQuery("from BehandlingVedtak where behandlingsresultat.id=:behandlingsresultatId", BehandlingVedtak.class); //$NON-NLS-1$
        query.setParameter(BEHANDLINGSRESULTAT_ID, behandlingsresultatId); // $NON-NLS-1$
        return query;
    }

    private static Optional<BehandlingVedtak> optionalFirstVedtak(List<BehandlingVedtak> behandlinger) {
        return behandlinger.isEmpty() ? Optional.empty() : Optional.of(behandlinger.get(0));
    }

}
