package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynDokumentEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class InnsynRepositoryImpl implements InnsynRepository {

    private EntityManager entityManager;

    InnsynRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public InnsynRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public void lagreInnsyn(Behandling behandling, InnsynEntitet innsyn, Collection<? extends InnsynDokument> innsynDokumenter) {
        entityManager.persist(innsyn);
        innsyn.getInnsynDokumenter().clear();

        innsynDokumenter.forEach(dok -> {
            InnsynDokumentEntitet entitet = new InnsynDokumentEntitet(dok.isFikkInnsyn(), dok.getJournalpostId(), dok.getDokumentId());
            entitet.setInnsyn(innsyn);
            innsyn.getInnsynDokumenter().add(entitet);
        });
        entityManager.persist(innsyn);
        entityManager.flush();
    }

    @Override
    public List<InnsynEntitet> hentForBehandling(long behandlingId) {
        TypedQuery<InnsynEntitet> query = entityManager.createQuery("from Innsyn where behandling.id = :behandlingId", InnsynEntitet.class);
        query.setParameter("behandlingId", behandlingId);
        return query.getResultList();
    }

    @Override
    public List<InnsynDokumentEntitet> hentDokumenterForInnsyn(long innsynId) {
        TypedQuery<InnsynDokumentEntitet> query = entityManager.createQuery("from InnsynDokument where innsyn.id=:innsynId", InnsynDokumentEntitet.class);
        query.setParameter("innsynId", innsynId);
        return query.getResultList();
    }
}
