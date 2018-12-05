package no.nav.foreldrepenger.behandlingslager.uttak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class UttakRepositoryImpl implements UttakRepository {

    private EntityManager entityManager;
    private BehandlingLåsRepository behandlingLåsRepository;

    public UttakRepositoryImpl() {
        //Jaja, sånn blir det
    }

    @Inject
    public UttakRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, BehandlingLåsRepository behandlingLåsRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.behandlingLåsRepository = behandlingLåsRepository;

    }

    public UttakRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        if (entityManager != null) {
            this.behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
        }
    }

    @Override
    public void lagreOpprinneligUttakResultatPerioder(Behandling behandling, UttakResultatPerioderEntitet opprinneligPerioder) {
        lagreUttaksresultat(behandling, builder -> builder.nullstill().medOpprinneligPerioder(opprinneligPerioder));
    }

    private void lagreUttaksresultat(Behandling behandling, Function<UttakResultatEntitet.Builder, UttakResultatEntitet.Builder> resultatTransformator) {
        verify(behandling);
        final BehandlingLås lås = behandlingLåsRepository.taLås(behandling.getId());

        Optional<UttakResultatEntitet> eksistrendeResultat = hentUttakResultatHvisEksisterer(behandling);

        UttakResultatEntitet.Builder builder = UttakResultatEntitet.builder(behandling);
        if (eksistrendeResultat.isPresent()) {
            UttakResultatEntitet eksisterende = eksistrendeResultat.get();
            if (eksisterende.getOpprinneligPerioder() != null) {
                builder.medOpprinneligPerioder(eksisterende.getOpprinneligPerioder());
            }
            if (eksisterende.getOverstyrtPerioder() != null) {
                builder.medOverstyrtPerioder(eksisterende.getOverstyrtPerioder());
            }
            deaktiverResultat(eksisterende);
        }
        builder = resultatTransformator.apply(builder);

        UttakResultatEntitet nyttResultat = builder.build();

        persistResultat(nyttResultat);
        verifiserBehandlingLås(lås);
        entityManager.flush();
    }

    private void persistResultat(UttakResultatEntitet resultat) {
        UttakResultatPerioderEntitet overstyrtPerioder = resultat.getOverstyrtPerioder();
        if (overstyrtPerioder != null) {
            persistPerioder(overstyrtPerioder);
        }
        UttakResultatPerioderEntitet opprinneligPerioder = resultat.getOpprinneligPerioder();
        if (opprinneligPerioder != null) {
            persistPerioder(opprinneligPerioder);
        }
        entityManager.persist(resultat);
    }

    private void persistPerioder(UttakResultatPerioderEntitet perioder) {
        entityManager.persist(perioder);
        for (UttakResultatPeriodeEntitet periode : perioder.getPerioder()) {
            persisterPeriode(periode);
        }
    }

    private void persisterPeriode(UttakResultatPeriodeEntitet periode) {
        if (periode.getPeriodeSøknad().isPresent()) {
            persistPeriodeSøknad(periode.getPeriodeSøknad().get());
        }
        entityManager.persist(periode);
        if (periode.getDokRegel() != null) {
            entityManager.persist(periode.getDokRegel());
        }
        for (UttakResultatPeriodeAktivitetEntitet periodeAktivitet : periode.getAktiviteter()) {
            persistAktivitet(periodeAktivitet);
        }
    }

    private void persistPeriodeSøknad(UttakResultatPeriodeSøknadEntitet periodeSøknad) {
        if (periodeSøknad != null) {
            entityManager.persist(periodeSøknad);
        }
    }

    private void persistAktivitet(UttakResultatPeriodeAktivitetEntitet periodeAktivitet) {
        persistUttakAktivitet(periodeAktivitet.getUttakAktivitet());
        entityManager.persist(periodeAktivitet);
    }

    private void persistUttakAktivitet(UttakAktivitetEntitet uttakAktivitet) {
        entityManager.persist(uttakAktivitet);
    }

    private void deaktiverResultat(UttakResultatEntitet resultat) {
        resultat.deaktiver();
        entityManager.persist(resultat);
        entityManager.flush();
    }

    private void verify(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(behandling.getBehandlingsresultat(), "behandling.behandlingsresultat"); // NOSONAR $NON-NLS-1$
    }

    @Override
    public Optional<UttakResultatEntitet> hentUttakResultatHvisEksisterer(Behandling behandling) {
        TypedQuery<UttakResultatEntitet> query = entityManager.createQuery(
            "select uttakResultat from UttakResultatEntitet uttakResultat " +
                "join uttakResultat.behandlingsresultat resultat" +
                " where resultat.behandling.id=:behandlingId and uttakResultat.aktiv='J'", UttakResultatEntitet.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandling.getId()); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    @Override
    public UttakResultatEntitet hentUttakResultat(Behandling behandling) {
        Optional<UttakResultatEntitet> resultat = hentUttakResultatHvisEksisterer(behandling);
        return resultat.orElseThrow(() -> new NoResultException("Fant ikke uttak resultat på behandlingen " + behandling.getId() + ", selv om det var forventet."));
    }

    private Optional<Uttaksperiodegrense> getAktivtUttaksperiodegrense(Behandlingsresultat behandlingsresultat) {
        Objects.requireNonNull(behandlingsresultat, "behandlingsresultat"); // NOSONAR $NON-NLS-1$
        final TypedQuery<Uttaksperiodegrense> query = entityManager.createQuery("FROM Uttaksperiodegrense Upg " +
            "WHERE Upg.behandlingsresultat.id = :behandlingresultatId " +
            "AND Upg.aktiv = :aktivt", Uttaksperiodegrense.class);
        query.setParameter("behandlingresultatId", behandlingsresultat.getId());
        query.setParameter("aktivt", true);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    @Override
    public void lagreUttaksperiodegrense(Behandling behandling, Uttaksperiodegrense uttaksperiodegrense) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        if (uttaksperiodegrense == null) {
            return;
        }
        final BehandlingLås lås = behandlingLåsRepository.taLås(behandling.getId());
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat == null) {
            throw new IllegalStateException("Finner ingen behandlingsresultat for behandling " + behandling.getId());
        }
        final Optional<Uttaksperiodegrense> tidligereAggregat = getAktivtUttaksperiodegrense(behandlingsresultat);
        if (tidligereAggregat.isPresent()) {
            final Uttaksperiodegrense aggregat = tidligereAggregat.get();
            boolean erForskjellig = uttaksperiodegrenseAggregatDiffer().areDifferent(aggregat, uttaksperiodegrense);
            if (erForskjellig) {
                aggregat.setAktiv(false);
                entityManager.persist(aggregat);
                entityManager.flush();
            }
        }
        behandlingsresultat.leggTilUttaksperiodegrense(uttaksperiodegrense);
        entityManager.persist(uttaksperiodegrense);
        verifiserBehandlingLås(lås);
        entityManager.flush();
    }

    private Optional<Long> finnAktivUttakId(Behandling behandling){
        return hentUttakResultatHvisEksisterer(behandling).map(UttakResultatEntitet::getId);
    }

    //Denne metoden bør legges i Tjeneste
    @Override
    public EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling){
        Optional<Long> funnetId = finnAktivUttakId(behandling);
        return funnetId
            .map(id-> EndringsresultatSnapshot.medSnapshot(UttakResultatEntitet.class,id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(UttakResultatEntitet.class));
    }


    private Optional<Long> finnAktivUttakPeriodeGrenseId(Behandling behandling){
        if(behandling.getBehandlingsresultat() == null){
            return Optional.empty();
        }
        return getAktivtUttaksperiodegrense(behandling.getBehandlingsresultat())
            .map(Uttaksperiodegrense::getId);
    }

    //Denne metoden bør legges i Tjeneste
    @Override
    public EndringsresultatSnapshot finnAktivUttakPeriodeGrenseAggregatId(Behandling behandling){
        Optional<Long> funnetId = finnAktivUttakPeriodeGrenseId(behandling);
        return funnetId
            .map(id-> EndringsresultatSnapshot.medSnapshot(Uttaksperiodegrense.class,id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(Uttaksperiodegrense.class));
    }

    @Override
    public Optional<Uttaksperiodegrense> hentUttaksperiodegrenseHvisEksisterer(Long behandlingId) {
        TypedQuery<Uttaksperiodegrense> query = entityManager
            .createQuery("select u from Uttaksperiodegrense u " +
                "where u.behandlingsresultat.behandling.id = :behandlingId " +
                "and u.aktiv = true", Uttaksperiodegrense.class)
            .setParameter("behandlingId", behandlingId);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    private DiffEntity uttaksperiodegrenseAggregatDiffer() {
        TraverseEntityGraph traverser = TraverseEntityGraphFactory.build(false);
        return new DiffEntity(traverser);
    }

    private void verifiserBehandlingLås(BehandlingLås lås) {
        behandlingLåsRepository.oppdaterLåsVersjon(lås);
    }
}
