package no.nav.foreldrepenger.behandlingslager.behandling.resultat.fravær;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FraværResultatRepositoryImpl implements FraværResultatRepository {

    private EntityManager entityManager;

    FraværResultatRepositoryImpl() {
        // CDI
    }

    @Inject
    public FraværResultatRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<FraværResultat> hentHvisEksistererFor(Behandlingsresultat behandlingsresultat) {
        return hentEksisterende(behandlingsresultat.getId());
    }

    @Override
    public FraværResultat hentFor(Behandlingsresultat behandlingsresultat) {
        return hentEksisterende(behandlingsresultat.getId()).orElseThrow(IllegalStateException::new);
    }

    private Optional<FraværResultat> hentEksisterende(Long behandlingsresultatId) {
        final TypedQuery<FraværResultatEntitet> query = entityManager.createQuery("FROM FraværResultat rs " +  // NOSONAR
            "WHERE rs.behandlingsresultat.id = :behandlingsresultatId " + //$NON-NLS-1$ //NOSONAR
            "AND rs.aktiv = true", FraværResultatEntitet.class);
        query.setParameter("behandlingsresultatId", behandlingsresultatId); // NOSONAR $NON-NLS-1$
        Optional<FraværResultatEntitet> entitet = query.getResultStream().findFirst();
        if (entitet.isPresent()) {
            return Optional.of(entitet.get());
        }
        return Optional.empty();
    }

    @Override
    public void lagre(Behandlingsresultat behandlingsresultat, FraværPerioderBuilder builder) {
        Objects.requireNonNull(behandlingsresultat, "behandlingsresultat");

        FraværPerioder fraværPerioder = builder != null ? builder.build() : null;
        FraværResultatEntitet entitet = new FraværResultatEntitet();

        Optional<FraværResultat> eksisterendeOpt = hentEksisterende(behandlingsresultat.getId());
        if (eksisterendeOpt.isPresent()) {
            FraværResultat eksisterende = eksisterendeOpt.get();
            entitet = new FraværResultatEntitet(eksisterende);
            entitet.setBehandlingsresultat(behandlingsresultat);
            entitet.setPerioder((FraværPerioderEntitet) fraværPerioder); // Må være satt for diff

            // diff først
            // hvis endring deaktiver eksisterende
            if (erIkkeEndring(eksisterende, entitet)) {
                return;
            }
            ((FraværResultatEntitet) eksisterende).setAktiv(false);
            entityManager.persist(eksisterende);
            entityManager.flush();
        }

        entitet.setBehandlingsresultat(behandlingsresultat);
        entitet.setPerioder((FraværPerioderEntitet) fraværPerioder);
        // lagre nytt

        lagreOgFlush(entitet);
    }

    private void lagreOgFlush(FraværResultatEntitet entitet) {
        FraværPerioder perioder = entitet.getPerioder();
        if (perioder != null) {
            entityManager.persist(perioder);
            for (FraværPeriode periode : perioder.getPerioder()) {
                entityManager.persist(periode);
            }
        }
        entityManager.persist(entitet);
        entityManager.flush();
    }

    private boolean erIkkeEndring(FraværResultat eksisterende, FraværResultatEntitet entitet) {
        TraverseEntityGraph traverseEntityGraph = TraverseEntityGraphFactory.build(false);
        DiffEntity diffEntity = new DiffEntity(traverseEntityGraph);
        return !diffEntity.areDifferent(eksisterende, entitet);
    }

    @Override
    public FraværPerioderBuilder opprettBuilder(Long behandlingsresultatId) {
        Optional<FraværResultat> fraværResultat = hentEksisterende(behandlingsresultatId);

        return FraværPerioderBuilder.oppdatere(fraværResultat.map(FraværResultat::getPerioder));
    }
}
