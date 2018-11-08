package no.nav.foreldrepenger.datavarehus;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class DatavarehusRepositoryImpl implements DatavarehusRepository {

    private EntityManager entityManager;

    DatavarehusRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public DatavarehusRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public long lagre(FagsakDvh fagsakDvh) {
        entityManager.persist(fagsakDvh);
        return fagsakDvh.getId();
    }

    @Override
    public long lagre(BehandlingDvh behandlingDvh) {
        entityManager.persist(behandlingDvh);
        return behandlingDvh.getId();
    }

    @Override
    public long lagre(BehandlingStegDvh behandlingStegDvh) {
        entityManager.persist(behandlingStegDvh);
        return behandlingStegDvh.getId();
    }

    @Override
    public long lagre(AksjonspunktDvh aksjonspunktDvh) {
        entityManager.persist(aksjonspunktDvh);
        return aksjonspunktDvh.getId();
    }

    @Override
    public long lagre(KontrollDvh kontrollDvh) {
        entityManager.persist(kontrollDvh);
        return kontrollDvh.getId();
    }

    @Override
    public long lagre(BehandlingVedtakDvh behandlingVedtakDvh) {
        entityManager.persist(behandlingVedtakDvh);
        return behandlingVedtakDvh.getId();
    }

    @Override
    public void lagre(VedtakUtbetalingDvh vedtakUtbetalingDvh) {
        entityManager.persist(vedtakUtbetalingDvh);
    }
}
