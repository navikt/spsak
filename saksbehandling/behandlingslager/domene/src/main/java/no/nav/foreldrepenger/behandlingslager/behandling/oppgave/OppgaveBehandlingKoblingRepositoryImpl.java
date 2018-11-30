package no.nav.foreldrepenger.behandlingslager.behandling.oppgave;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class OppgaveBehandlingKoblingRepositoryImpl implements OppgaveBehandlingKoblingRepository {

    private EntityManager entityManager;

    OppgaveBehandlingKoblingRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public OppgaveBehandlingKoblingRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public Long lagre(OppgaveBehandlingKobling oppgaveBehandlingKobling) {
        entityManager.persist(oppgaveBehandlingKobling);
        entityManager.flush();
        return oppgaveBehandlingKobling.getId();
    }

    @Override
    public Optional<OppgaveBehandlingKobling> hentOppgaveBehandlingKobling(String oppgaveId) {
        TypedQuery<OppgaveBehandlingKobling> query = entityManager.createQuery("from OppgaveBehandlingKobling where oppgave_id=:oppgaveId", //$NON-NLS-1$
            OppgaveBehandlingKobling.class);
        query.setParameter("oppgaveId", oppgaveId); //$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(query);
    }

    @Override
    public List<OppgaveBehandlingKobling> hentOppgaverRelatertTilBehandling(Long behandlingId) {
        TypedQuery<OppgaveBehandlingKobling> query = entityManager.createQuery("from OppgaveBehandlingKobling where behandling_id=:behandlingId", //$NON-NLS-1$
            OppgaveBehandlingKobling.class);
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return query.getResultList();
    }
}
