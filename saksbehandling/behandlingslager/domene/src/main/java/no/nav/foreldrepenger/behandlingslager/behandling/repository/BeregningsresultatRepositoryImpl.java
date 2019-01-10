package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BeregningsresultatRepositoryImpl implements BeregningsresultatRepository {
    private EntityManager entityManager;
    private BehandlingLåsRepository behandlingLåsRepository;

    BeregningsresultatRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BeregningsresultatRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
    }

    @Override
    public Optional<BeregningsresultatPerioder> hentHvisEksisterer(Behandlingsresultat behandlingsresultat) {
        return hentHvisEksistererFor(behandlingsresultat).map(BeregningsResultat::getBeregningsresultat);
    }

    @Override
    public Optional<BeregningsResultat> hentHvisEksistererFor(Behandlingsresultat behandlingsresultat) {
        TypedQuery<BeregningsResultat> query = entityManager.createQuery(
            "from BeregningsResultat kobling " +
                "where kobling.behandlingsresultat.id=:behandlingId and kobling.aktiv = true", BeregningsResultat.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingsresultat.getId()); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    @Override
    public long lagre(Behandlingsresultat behandlingsresultat, BeregningsresultatPerioder beregningsresultat) {
        Objects.requireNonNull(behandlingsresultat, "behandlingsresultat");
        entityManager.persist(beregningsresultat);
        Optional<BeregningsResultat> beregningsresultatFPKoblingOptional = hentHvisEksistererFor(behandlingsresultat);
        if (!beregningsresultatFPKoblingOptional.isPresent()) {
            BeregningsResultat kobling = new BeregningsResultat(behandlingsresultat, beregningsresultat);
            entityManager.persist(kobling);
        }
        beregningsresultat.getBeregningsresultatPerioder().forEach(this::lagre);
        entityManager.flush();
        return beregningsresultat.getId();
    }

    private void lagre(BeregningsresultatPeriode beregningsresultatPeriode) {
        entityManager.persist(beregningsresultatPeriode);
        beregningsresultatPeriode.getBeregningsresultatAndelList().forEach(this::lagre);
    }

    private void lagre(BeregningsresultatAndel beregningsresultatAndel) {
        entityManager.persist(beregningsresultatAndel);
    }

    @Override
    public void deaktiverBeregningsresultat(Behandlingsresultat behandlingsresultat, BehandlingLås skriveLås) {
        Optional<BeregningsResultat> koblingOpt = hentHvisEksistererFor(behandlingsresultat);
        koblingOpt.ifPresent(kobling -> setAktivOgLagre(kobling, false));
        verifiserBehandlingLås(skriveLås);
        entityManager.flush();
    }

    private void setAktivOgLagre(BeregningsResultat kobling, boolean aktiv) {
        kobling.setAktiv(aktiv);
        entityManager.persist(kobling);
        entityManager.flush();
    }

    private void verifiserBehandlingLås(BehandlingLås lås) {
        behandlingLåsRepository.oppdaterLåsVersjon(lås);
    }
}
