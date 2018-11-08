package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class MottatteDokumentRepositoryImpl implements MottatteDokumentRepository {

    private EntityManager entityManager;

    private static final String PARAM_KEY = "param";

    public MottatteDokumentRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public MottatteDokumentRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public MottattDokument lagre(MottattDokument mottattDokument) {
        entityManager.persist(mottattDokument);
        entityManager.flush();

        return mottattDokument;
    }

    @Override
    public Optional<MottattDokument> hentMottattDokument(long mottattDokumentId) {
        TypedQuery<MottattDokument> query = entityManager.createQuery(
            "select m from MottattDokument m where m.id = :param", MottattDokument.class)
            .setParameter(PARAM_KEY, mottattDokumentId);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    @Override
    public List<MottattDokument> hentMottatteDokument(long behandlingId) {
        String strQueryTemplate = "select m from MottattDokument m where m.behandlingId = :param";
        return entityManager.createQuery(
            strQueryTemplate, MottattDokument.class)
            .setParameter(PARAM_KEY, behandlingId)
            .getResultList();
    }

    @Override
    public List<MottattDokument> hentMottatteDokumentMedFagsakId(long fagsakId) {
        String strQueryTemplate = "select m from MottattDokument m where m.fagsakId = :param";
        return entityManager.createQuery(
            strQueryTemplate, MottattDokument.class)
            .setParameter(PARAM_KEY, fagsakId)
            .getResultList();
    }

    @Override
    public List<MottattDokument> hentMottatteDokumentMedForsendelseId(UUID forsendelseId) {
        String strQueryTemplate = "select m from MottattDokument m where m.forsendelseId = :param";
        return entityManager.createQuery(
            strQueryTemplate, MottattDokument.class)
            .setParameter(PARAM_KEY, forsendelseId)
            .getResultList();
    }

    @Override
    public List<MottattDokument> hentMottatteDokumentVedleggPåBehandlingId(long behandlingId) {
        TypedQuery<MottattDokument> query = entityManager.createQuery(
            "SELECT md FROM MottattDokument md WHERE md.behandlingId = :behandlingId AND md.dokumentTypeId IN :dokumentTyper", //$NON-NLS-1$
            MottattDokument.class)
            .setParameter("dokumentTyper", DokumentTypeId.getVedleggTyper())
            .setParameter("behandlingId", behandlingId); //$NON-NLS-1$

        return query.getResultList();
    }

    @Override
    public List<MottattDokument> hentMottatteDokumentAndreTyperPåBehandlingId(long behandlingId) {
        TypedQuery<MottattDokument> query = entityManager.createQuery(
            "SELECT md FROM MottattDokument md WHERE md.behandlingId = :behandlingId " +
                "AND md.dokumentTypeId NOT IN :dokumentTyper " +
                "AND md.dokumentTypeId <> :udefinert", //$NON-NLS-1$
            MottattDokument.class)
            .setParameter("dokumentTyper", DokumentTypeId.getSpesialTyper())
            .setParameter("udefinert", DokumentTypeId.UDEFINERT)
            .setParameter("behandlingId", behandlingId); //$NON-NLS-1$

        return query.getResultList();
    }

    @Override
    public void oppdaterMedBehandling(MottattDokument mottattDokument, long behandlingId) {
        entityManager.createQuery(
            "update MottattDokument set behandlingId = :behandlingId WHERE id = :dokumentId")
            .setParameter("dokumentId", mottattDokument.getId())
            .setParameter("behandlingId", behandlingId)
            .executeUpdate();
    }
}
