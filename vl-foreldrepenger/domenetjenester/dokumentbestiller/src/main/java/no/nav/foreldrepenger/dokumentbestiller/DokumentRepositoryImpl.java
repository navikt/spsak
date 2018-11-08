package no.nav.foreldrepenger.dokumentbestiller;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentAdresse;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class DokumentRepositoryImpl implements DokumentRepository {

    private EntityManager entityManager;

    DokumentRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public DokumentRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Long lagre(DokumentData dokumentData) {
        entityManager.persist(dokumentData);
        for (DokumentFelles df : dokumentData.getDokumentFelles()) {
            entityManager.persist(df);
        }
        entityManager.flush();
        return dokumentData.getId();
    }

    // FIXME (ONYX): Kan denne konsolideres med lagre(DokumentData)? SEr ut til å kalles i samme kontekst, men under
    // bygging av objektgrafen. Bør kunne håndtere via cascade? eller er tanken at Adresse skal normaliseres uavhengig
    // av dokument?
    @Override
    public Long lagre(DokumentAdresse adresse) {
        entityManager.persist(adresse);
        return adresse.getId();
    }

    @Override
    public DokumentData hentDokumentData(Long dokumentDataId) {
        return entityManager.find(DokumentData.class, dokumentDataId);
    }

    @Override
    public List<DokumentMalType> hentAlleDokumentMalTyper() {
        return entityManager.createQuery("SELECT d FROM DokumentMalType d", DokumentMalType.class) //$NON-NLS-1$
            .setHint(QueryHints.HINT_READONLY, "true") //$NON-NLS-1$
            .getResultList();
    }

    @Override
    public DokumentMalType hentDokumentMalType(String kode) {
        TypedQuery<DokumentMalType> query = entityManager
            .createQuery("from DokumentMalType d where d.kode = :kode", DokumentMalType.class)
            .setParameter("kode", kode);
        return HibernateVerktøy.hentEksaktResultat(query);
    }

    @Override
    public List<DokumentData> hentDokumentDataListe(Long behandlingId, String dokumentmal) {
        TypedQuery<DokumentData> query = entityManager
            .createQuery("from DokumentData dd where dd.behandling.id = :behandlingId and dd.dokumentMalType.kode = :dokumentmal", DokumentData.class)
            .setParameter("behandlingId", behandlingId)
            .setParameter("dokumentmal", dokumentmal);

        return query.getResultList();
    }
}
