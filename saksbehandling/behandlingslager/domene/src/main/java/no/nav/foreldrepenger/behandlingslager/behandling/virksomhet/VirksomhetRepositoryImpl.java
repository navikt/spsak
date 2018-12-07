package no.nav.foreldrepenger.behandlingslager.behandling.virksomhet;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VirksomhetRepositoryImpl implements VirksomhetRepository {

    private EntityManager entityManager;
    private Logger logger = LoggerFactory.getLogger(VirksomhetRepositoryImpl.class);

    public VirksomhetRepositoryImpl() {
        // CDI
    }

    @Inject
    public VirksomhetRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Virksomhet> hent(String orgnr) {
        final TypedQuery<VirksomhetEntitet> query = entityManager.createQuery("FROM Virksomhet WHERE orgnr = :orgnr", VirksomhetEntitet.class);
        query.setParameter("orgnr", orgnr);
        final Optional<VirksomhetEntitet> virksomhetEntitet = HibernateVerktøy.hentUniktResultat(query);
        if (virksomhetEntitet.isPresent()) {
            VirksomhetEntitet entitet = virksomhetEntitet.get();
            entityManager.detach(entitet);
            return Optional.of(entitet);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Virksomhet> hentForEditering(String orgnr) {
        final TypedQuery<VirksomhetEntitet> query = entityManager.createQuery("FROM Virksomhet WHERE orgnr = :orgnr", VirksomhetEntitet.class);
        query.setParameter("orgnr", orgnr);
        final Optional<VirksomhetEntitet> virksomhetEntitet = HibernateVerktøy.hentUniktResultat(query);
        if (virksomhetEntitet.isPresent()) {
            VirksomhetEntitet entitet = virksomhetEntitet.get();
            return Optional.of(entitet);
        }
        return Optional.empty();
    }

    @Override
    public void lagre(Virksomhet virksomhet) {
        entityManager.persist(virksomhet);
        try {
            entityManager.flush();
        } catch (PersistenceException exception) {
            if (exception.getCause() instanceof ConstraintViolationException) {
                logger.info("Prøver å lagre duplikat virksomhet={}.", virksomhet);
                throw new VirksomhetAlleredeLagretException();
            }
            throw exception;
        }
        entityManager.detach(virksomhet);
    }

}
