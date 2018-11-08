package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFPKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BeregningsresultatFPRepositoryImpl implements BeregningsresultatFPRepository {
    private EntityManager entityManager;
    private BehandlingLåsRepository behandlingLåsRepository;

    BeregningsresultatFPRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BeregningsresultatFPRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
    }

    @Override
    public Optional<BeregningsresultatFP> hentBeregningsresultatFP(Behandling behandling) {
        return hentBeregningsresultatFPKobling(behandling).map(BeregningsresultatFPKobling::getBeregningsresultatFP);
    }

    @Override
    public Optional<BeregningsresultatFPKobling> hentBeregningsresultatFPKobling(Behandling behandling) {
        TypedQuery<BeregningsresultatFPKobling> query = entityManager.createQuery(
            "from BeregningsresultatFPKobling kobling " +
                "where kobling.behandling.id=:behandlingId and kobling.aktiv = 'J'", BeregningsresultatFPKobling.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandling.getId()); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    @Override
    public long lagre(Behandling behandling, BeregningsresultatFP beregningsresultatFP) {
        entityManager.persist(beregningsresultatFP);
        Optional<BeregningsresultatFPKobling> beregningsresultatFPKoblingOptional = hentBeregningsresultatFPKobling(behandling);
        if (!beregningsresultatFPKoblingOptional.isPresent()) {
            BeregningsresultatFPKobling kobling = new BeregningsresultatFPKobling(behandling, beregningsresultatFP);
            entityManager.persist(kobling);
        }
        beregningsresultatFP.getBeregningsresultatPerioder().forEach(this::lagre);
        entityManager.flush();
        return beregningsresultatFP.getId();
    }

    private void lagre(BeregningsresultatPeriode beregningsresultatPeriode) {
        entityManager.persist(beregningsresultatPeriode);
        beregningsresultatPeriode.getBeregningsresultatAndelList().forEach(this::lagre);
    }

    private void lagre(BeregningsresultatAndel beregningsresultatAndel) {
        entityManager.persist(beregningsresultatAndel);
    }

    @Override
    public void deaktiverBeregningsresultatFP(Behandling behandling, BehandlingLås skriveLås) {
        Optional<BeregningsresultatFPKobling> koblingOpt = hentBeregningsresultatFPKobling(behandling);
        koblingOpt.ifPresent(kobling -> setAktivOgLagre(kobling, false));
        verifiserBehandlingLås(skriveLås);
        entityManager.flush();
    }

    private void setAktivOgLagre(BeregningsresultatFPKobling kobling, boolean aktiv) {
        kobling.setAktiv(aktiv);
        entityManager.persist(kobling);
        entityManager.flush();
    }

    private void verifiserBehandlingLås(BehandlingLås lås) {
        behandlingLåsRepository.oppdaterLåsVersjon(lås);
    }
}
