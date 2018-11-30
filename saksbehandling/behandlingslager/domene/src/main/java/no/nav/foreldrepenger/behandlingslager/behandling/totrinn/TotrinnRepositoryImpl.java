package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;


import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class TotrinnRepositoryImpl implements TotrinnRepository {

    private EntityManager entityManager;

    TotrinnRepositoryImpl() {
        // CDI
    }

    @Inject
    public TotrinnRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public void lagreTotrinnsresultatgrunnlag(Behandling behandling, Totrinnresultatgrunnlag totrinnresultatgrunnlag) {
        entityManager.persist(totrinnresultatgrunnlag);
    }

    @Override
    public void lagreTotrinnaksjonspunktvurdering(Totrinnsvurdering totrinnsvurdering) {
        entityManager.persist(totrinnsvurdering);
    }

    @Override
    public void lagreOgFlush(Behandling behandling, Totrinnresultatgrunnlag totrinnresultatgrunnlag) {
        Objects.requireNonNull(behandling, "behandling");

        Optional<Totrinnresultatgrunnlag> aktivtTotrinnresultatgrunnlag = getAktivtTotrinnresultatgrunnlag(behandling);
        if (aktivtTotrinnresultatgrunnlag.isPresent()) {
            Totrinnresultatgrunnlag grunnlag = aktivtTotrinnresultatgrunnlag.get();
            grunnlag.setAktiv(false);
            entityManager.persist(grunnlag);
        }
        lagreTotrinnsresultatgrunnlag(behandling, totrinnresultatgrunnlag);
        entityManager.flush();
    }

    @Override
    public void lagreOgFlush(Behandling behandling, Collection<Totrinnsvurdering> totrinnaksjonspunktvurderinger) {
        Objects.requireNonNull(behandling, "behandling");

        Collection<Totrinnsvurdering> aktiveVurderinger = getAktiveTotrinnaksjonspunktvurderinger(behandling);
        if (!aktiveVurderinger.isEmpty()) {
            aktiveVurderinger.forEach(vurdering -> {
                vurdering.setAktiv(false);
                entityManager.persist(vurdering);
            });
        }
        totrinnaksjonspunktvurderinger.forEach(this::lagreTotrinnaksjonspunktvurdering);
        entityManager.flush();
    }


    @Override
    public Optional<Totrinnresultatgrunnlag> hentTotrinngrunnlag(Behandling behandling) {
        return getAktivtTotrinnresultatgrunnlag(behandling);
    }

    @Override
    public Collection<Totrinnsvurdering> hentTotrinnaksjonspunktvurderinger(Behandling behandling) {
        return getAktiveTotrinnaksjonspunktvurderinger(behandling);
    }

    protected Optional<Totrinnresultatgrunnlag> getAktivtTotrinnresultatgrunnlag(Behandling behandling) {
        return getAktivtTotrinnresultatgrunnlag(behandling.getId());
    }

    protected Optional<Totrinnresultatgrunnlag> getAktivtTotrinnresultatgrunnlag(Long behandlingId) {
        TypedQuery<Totrinnresultatgrunnlag> query = entityManager.createQuery(
            "SELECT trg FROM Totrinnresultatgrunnlag trg WHERE trg.behandling.id = :behandling_id AND trg.aktiv = 'J'", //$NON-NLS-1$
            Totrinnresultatgrunnlag.class);

        query.setParameter("behandling_id", behandlingId); //$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(query);
    }

    protected Collection<Totrinnsvurdering> getAktiveTotrinnaksjonspunktvurderinger(Behandling behandling) {
        return getAktiveTotrinnaksjonspunktvurderinger(behandling.getId());
    }

    protected Collection<Totrinnsvurdering> getAktiveTotrinnaksjonspunktvurderinger(Long behandlingId) {
        TypedQuery<Totrinnsvurdering> query = entityManager.createQuery(
            "SELECT tav FROM Totrinnsvurdering tav WHERE tav.behandling.id = :behandling_id AND tav.aktiv = 'J'", //$NON-NLS-1$
            Totrinnsvurdering.class);

        query.setParameter("behandling_id", behandlingId); //$NON-NLS-1$
        return query.getResultList();
    }
}
