package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static java.util.Arrays.asList;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling.Builder;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BehandlingRepositoryImpl implements BehandlingRepository {

    private EntityManager entityManager;

    @Inject
    public BehandlingRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    BehandlingRepositoryImpl() {
    }

    private static Optional<Behandling> optionalFirst(List<Behandling> behandlinger) {
        return behandlinger.isEmpty() ? Optional.empty() : Optional.of(behandlinger.get(0));
    }

    EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Optional<Behandling> finnUnikBehandlingForBehandlingId(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(lagBehandlingQuery(behandlingId));
    }

    @Override
    public Behandling hentBehandling(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$
        return hentEksaktResultat(lagBehandlingQuery(behandlingId));
    }

    @Override
    public Optional<Behandlingsresultat> hentResultatHvisEksisterer(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$
        TypedQuery<Behandlingsresultat> query = getEntityManager().createQuery("from Behandlingsresultat where behandling.id=:behandlingId", Behandlingsresultat.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(query);
    }

    @Override
    public Behandlingsresultat hentResultat(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$
        TypedQuery<Behandlingsresultat> query = getEntityManager().createQuery("from Behandlingsresultat where behandling.id=:behandlingId", Behandlingsresultat.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return HibernateVerktøy.hentEksaktResultat(query);
    }

    @Override
    public List<Behandling> hentAbsoluttAlleBehandlingerForSaksnummer(Saksnummer saksnummer) {
        Objects.requireNonNull(saksnummer, "saksnummer"); //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "SELECT beh from Behandling AS beh, Fagsak AS fagsak WHERE beh.fagsak.id=fagsak.id AND fagsak.saksnummer=:saksnummer", //$NON-NLS-1$
            Behandling.class);
        query.setParameter("saksnummer", saksnummer); //$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public Optional<Behandling> hentSisteBehandlingForFagsakId(Long fagsakId) {
        return finnSisteBehandling(fagsakId, false);
    }

    @Override
    public Optional<Behandling> hentSisteBehandlingForFagsakId(Long fagsakId, BehandlingType behandlingType) {
        return finnSisteBehandling(fagsakId, behandlingType, false);
    }

    @Override
    public Optional<Behandling> hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(Long fagsakId, List<BehandlingType> behandlingType) {
        return finnSisteBehandlingEkskluderBehandlingType(fagsakId, behandlingType, false);
    }

    @Override
    public List<Behandling> hentBehandlingerMedÅrsakerForFagsakId(Long fagsakId, Set<BehandlingÅrsakType> årsaker) {
        TypedQuery<Behandling> query = getEntityManager().createQuery("SELECT b FROM Behandling b" +
            " WHERE b.fagsak.id = :fagsakId " +
            " AND EXISTS (SELECT å FROM BehandlingÅrsak å" +
            "   WHERE å.behandling = b AND å.behandlingÅrsakType IN :årsaker)", Behandling.class);
        query.setParameter("fagsakId", fagsakId);
        query.setParameter("årsaker", årsaker);

        return query.getResultList();
    }

    @Override
    public List<Behandling> hentBehandlingerSomIkkeErAvsluttetForFagsakId(Long fagsakId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "SELECT beh from Behandling AS beh WHERE beh.fagsak.id = :fagsakId AND beh.status != :status", //$NON-NLS-1$
            Behandling.class);
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        query.setParameter("status", BehandlingStatus.AVSLUTTET); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public List<Behandling> hentÅpneBehandlingerForFagsakId(Long fagsakId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "SELECT beh from Behandling AS beh " +
                "WHERE beh.fagsak.id = :fagsakId " +
                "AND beh.status NOT IN (:status)", //$NON-NLS-1$
            Behandling.class);
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        query.setParameter("status", asList(BehandlingStatus.AVSLUTTET, BehandlingStatus.IVERKSETTER_VEDTAK)); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public Long lagre(Behandling behandling, BehandlingLås lås) {
        if (!Objects.equals(behandling.getId(), lås.getBehandlingId())) {
            // hvis satt må begge være like. (Objects.equals håndterer også at begge er null)
            throw new IllegalArgumentException(
                "Behandling#id [" + behandling.getId() + "] og lås#behandlingId [" + lås.getBehandlingId() + "] må være like, eller begge må være null."); //$NON-NLS-1$
        }

        long behandlingId = lagre(behandling);
        verifiserBehandlingLås(lås);

        // i tilfelle denne ikke er satt fra før, f.eks. for ny entitet
        lås.setBehandlingId(behandlingId);

        return behandlingId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Behandling> finnSisteAvsluttedeIkkeHenlagteBehandling(Long fagsakId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$

        Query query = getEntityManager().createQuery(
            " FROM Behandling b WHERE b.fagsak.id=:fagsakId " +
                " AND b.status IN :avsluttetOgIverkKode" +
                " AND NOT EXISTS (SELECT r FROM Behandlingsresultat r" +
                "    WHERE r.behandling=b " +
                "    AND r.behandlingResultatType IN :henlagtKoder)" +
                " ORDER BY b.opprettetTidspunkt DESC" //$NON-NLS-1$
        );
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        query.setParameter("avsluttetOgIverkKode", asList(BehandlingStatus.AVSLUTTET, BehandlingStatus.IVERKSETTER_VEDTAK));
        query.setParameter("henlagtKoder", BehandlingResultatType.getAlleHenleggelseskoder());

        return optionalFirst(query.getResultList());
    }

    @Override
    public Long lagre(VilkårResultat vilkårResultat, BehandlingLås lås) {
        long id = lagre(vilkårResultat);
        verifiserBehandlingLås(lås);
        getEntityManager().flush();
        return id;
    }

    @Override
    public Long lagre(Behandlingsresultat behandlingsresultats, BehandlingLås lås) {
        Long id = lagre(behandlingsresultats);
        verifiserBehandlingLås(lås);
        getEntityManager().flush();
        return id;
    }

    @Override
    public BehandlingLås taSkriveLås(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$
        BehandlingLåsRepositoryImpl låsRepo = new BehandlingLåsRepositoryImpl(getEntityManager());

        return låsRepo.taLås(behandling.getId());
    }

    @Override
    public BehandlingStegType finnBehandlingStegType(String kode) {
        return getEntityManager().find(BehandlingStegType.class, kode);
    }

    private Optional<Behandling> finnSisteBehandlingEkskluderBehandlingType(Long fagsakId, List<BehandlingType> behandlingType,
                                                                            boolean readOnly) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(behandlingType, "behandlingType"); // NOSONAR //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "from Behandling where fagsak.id=:fagsakId and behandlingType not in :behandlingType order by opprettetTidspunkt desc", //$NON-NLS-1$
            Behandling.class);
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        query.setParameter("behandlingType", behandlingType); //$NON-NLS-1$
        if (readOnly) {
            query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        }
        return optionalFirst(query.getResultList());
    }

    private Optional<Behandling> finnSisteBehandling(Long fagsakId, BehandlingType behandlingType, boolean readOnly) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(behandlingType, "behandlingType"); // NOSONAR //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "from Behandling where fagsak.id=:fagsakId and behandlingType=:behandlingType order by opprettetTidspunkt desc", //$NON-NLS-1$
            Behandling.class);
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        query.setParameter("behandlingType", behandlingType); //$NON-NLS-1$
        if (readOnly) {
            query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        }
        return optionalFirst(query.getResultList());
    }

    private Optional<Behandling> finnSisteBehandling(Long fagsakId, boolean readOnly) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "from Behandling where fagsak.id=:fagsakId order by opprettetTidspunkt desc", //$NON-NLS-1$
            Behandling.class);
        query.setParameter("fagsakId", fagsakId); //$NON-NLS-1$
        if (readOnly) {
            query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        }
        return optionalFirst(query.getResultList());
    }

    private IllegalStateException flereAggregatOpprettelserISammeLagringException(Class<?> aggregat) {
        return new IllegalStateException("Glemt å lagre " // NOSONAR //$NON-NLS-1$
            + aggregat.getSimpleName()
            + "? Denne må lagres separat siden den er et selvstendig aggregat delt mellom behandlinger"); //$NON-NLS-1$
    }

    private TypedQuery<Behandling> lagBehandlingQuery(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery("from Behandling where id=:behandlingId", Behandling.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return query;
    }

    private Long lagre(VilkårResultat vilkårResultat) {
        Behandling originalBehandling = vilkårResultat.getOriginalBehandling();

        if (originalBehandling == null || originalBehandling.getId() == null) {
            throw new IllegalStateException("Glemt å lagre " // NOSONAR //$NON-NLS-1$
                + Behandling.class.getSimpleName()
                + "? Denne må lagres separat siden "// NOSONAR //$NON-NLS-1$
                + VilkårResultat.class.getSimpleName()
                + " er et separat aggregat delt mellom flere behandlinger"); //$NON-NLS-1$ // NOSONAR
        }

        getEntityManager().persist(originalBehandling);
        getEntityManager().persist(vilkårResultat);
        for (Vilkår ivr : vilkårResultat.getVilkårene()) {
            getEntityManager().persist(ivr);
            getEntityManager().persist(ivr.getVilkårResultat());
            for (Vilkår vr : ivr.getVilkårResultat().getVilkårene()) {
                getEntityManager().persist(vr);
            }
        }
        return vilkårResultat.getId();
    }

    // sjekk lås og oppgrader til skriv
    @Override
    public void verifiserBehandlingLås(BehandlingLås lås) {
        BehandlingLåsRepositoryImpl låsHåndterer = new BehandlingLåsRepositoryImpl(getEntityManager());
        låsHåndterer.oppdaterLåsVersjon(lås);
    }

    Long lagre(Behandling behandling) {
        getEntityManager().persist(behandling);

        List<BehandlingÅrsak> behandlingÅrsak = behandling.getBehandlingÅrsaker();
        behandlingÅrsak.forEach(getEntityManager()::persist);

        getEntityManager().flush();

        return behandling.getId();
    }

    Long lagre(Behandlingsresultat behandlingsresultat) {
        getEntityManager().persist(behandlingsresultat);

        VilkårResultat vilkårResultat = behandlingsresultat.getVilkårResultat();
        if (vilkårResultat != null && vilkårResultat.getId() == null) {
            throw flereAggregatOpprettelserISammeLagringException(VilkårResultat.class);
        }
        getEntityManager().flush();
        return behandlingsresultat.getId();
    }

    @Override
    public Boolean erVersjonUendret(Long behandlingId, Long versjon) {
        Query query = getEntityManager().createNativeQuery(
            "SELECT COUNT(*) " +
                "WHERE exists (SELECT 1 FROM behandling WHERE (behandling.id = ?) AND (behandling.versjon = ?))");
        query.setParameter(1, behandlingId);
        query.setParameter(2, versjon);
        return ((BigInteger) query.getSingleResult()).intValue() == 1;
    }

    @Override
    public Behandling opprettNyBehandlingBasertPåTidligere(Behandling gammelBehandling, BehandlingType behandlingType,
                                                           GrunnlagRepositoryProvider repositoryProvider) {
        // ta lås på gammel behandling først
        taSkriveLås(gammelBehandling);

        // opprett så ny
        Builder nyBuilder = Behandling.nyBehandlingFor(gammelBehandling.getFagsak(), behandlingType);

        Behandling nyBehandling = nyBuilder.build();
        BehandlingLås lås = taSkriveLås(nyBehandling);
        lagre(nyBehandling, lås);

        repositoryProvider.getMedlemskapRepository().kopierGrunnlagFraEksisterendeBehandling(gammelBehandling, nyBehandling);
        repositoryProvider.getPersonopplysningRepository().kopierGrunnlagFraEksisterendeBehandling(gammelBehandling, nyBehandling);

        return nyBehandling;
    }

    @Override
    public void oppdaterSistOppdatertTidspunkt(Behandling behandling, LocalDateTime tidspunkt) {
        Query query = getEntityManager().createNativeQuery("UPDATE BEHANDLING SET SIST_OPPDATERT_TIDSPUNKT = :tidspunkt WHERE " +
            "ID = :behandling_id");

        query.setParameter("tidspunkt", tidspunkt); // NOSONAR $NON-NLS-1$
        query.setParameter("behandling_id", behandling.getId()); // NOSONAR $NON-NLS-1$

        query.executeUpdate();
    }

    @Override
    public Optional<LocalDateTime> hentSistOppdatertTidspunkt(Behandling behandling) {
        Query query = getEntityManager().createNativeQuery("SELECT be.SIST_OPPDATERT_TIDSPUNKT FROM BEHANDLING be WHERE be.ID = :behandling_id");

        query.setParameter("behandling_id", behandling.getId()); // NOSONAR $NON-NLS-1$

        Object resultat = query.getSingleResult();
        if (resultat == null) {
            return Optional.empty();
        }

        Timestamp timestamp = (Timestamp) resultat;
        LocalDateTime value = LocalDateTime.ofInstant(timestamp.toInstant(), TimeZone.getDefault().toZoneId());
        return Optional.of(value);
    }

    @Override
    public List<BehandlingÅrsak> finnÅrsakerForBehandling(Behandling behandling) {
        TypedQuery<BehandlingÅrsak> query = entityManager.createQuery(
            "FROM BehandlingÅrsak  årsak " +
                "WHERE (årsak.behandling = :behandling)",
            BehandlingÅrsak.class);

        query.setParameter("behandling", behandling);
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public List<BehandlingÅrsakType> finnÅrsakTyperForBehandling(Behandling behandling) {
        TypedQuery<BehandlingÅrsakType> query = entityManager.createQuery(
            "select distinct behandlingÅrsakType FROM BehandlingÅrsak  årsak " +
                "WHERE årsak.behandling = :behandling",
            BehandlingÅrsakType.class);

        query.setParameter("behandling", behandling);
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return query.getResultList();
    }
}
