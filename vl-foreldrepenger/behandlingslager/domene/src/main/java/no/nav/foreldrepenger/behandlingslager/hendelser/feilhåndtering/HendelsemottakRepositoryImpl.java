package no.nav.foreldrepenger.behandlingslager.hendelser.feilhåndtering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class HendelsemottakRepositoryImpl implements HendelsemottakRepository {

    private EntityManager entityManager;

    HendelsemottakRepositoryImpl() {
        // CDI
    }

    @Inject
    public HendelsemottakRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean hendelseErNy(String hendelseUid) {
        TypedQuery<MottattHendelse> query = entityManager.createQuery("from MottattHendelse where hendelse_uid=:hendelse_uid", MottattHendelse.class);
        query.setParameter("hendelse_uid", hendelseUid);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getResultList().isEmpty();
    }

    @Override
    public void registrerMottattHendelse(String hendelseUid) {
        Query query = entityManager.createNativeQuery("INSERT INTO MOTTATT_HENDELSE (hendelse_uid) VALUES (:hendelse_uid)");
        query.setParameter("hendelse_uid", hendelseUid);
        query.executeUpdate();
    }

}
