package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge;

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
public class VergeRepositoryImpl implements VergeRepository {
    private EntityManager entityManager;

    @Inject
    public VergeRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<VergeAggregat> hentAggregat(Behandling behandling) {
        return hentVerge(getAktivtBehandlingsgrunnlag(behandling.getId()));
    }

    @Override
    public Optional<VergeAggregat> hentAggregat(Long behandlingId) {
        return hentVerge(getAktivtBehandlingsgrunnlag(behandlingId));
    }

    @Override
    public void lagreOgFlush(Behandling behandling, VergeBuilder vergeBuilder) {
        Objects.requireNonNull(behandling, "behandling");

        Optional<VergeGrunnlagEntitet> aktivtBehandlingsgrunnlagOpt = getAktivtBehandlingsgrunnlag(behandling);
        if (aktivtBehandlingsgrunnlagOpt.isPresent()) {
            VergeGrunnlagEntitet grunnlag = aktivtBehandlingsgrunnlagOpt.get();
            grunnlag.setAktiv(false);
            entityManager.persist(grunnlag);
            entityManager.flush();
            lagreGrunnlag(behandling, vergeBuilder);
        } else {
            lagreGrunnlag(behandling, vergeBuilder);
        }
        entityManager.flush();
    }


    private void lagreVerge(VergeEntitet verge) {
        entityManager.persist(verge);
    }

    private void lagreGrunnlag(Behandling behandling, VergeBuilder builder) {
        VergeEntitet verge = builder.build();
        lagreVerge(verge);
        VergeGrunnlagEntitet grunnlag = new VergeGrunnlagEntitet(behandling, verge);
        entityManager.persist(grunnlag);
    }

    private Optional<VergeAggregat> hentVerge(Optional<VergeGrunnlagEntitet> optGrunnlag) {
        if (optGrunnlag.isPresent()) {
            VergeGrunnlagEntitet grunnlag = optGrunnlag.get();
            VergeAggregat vergeAggregat = grunnlag.tilAggregat();
            return Optional.of(vergeAggregat);
        } else {
            return Optional.empty();
        }
    }

    protected Optional<VergeGrunnlagEntitet> getAktivtBehandlingsgrunnlag(Behandling behandling) {
        return getAktivtBehandlingsgrunnlag(behandling.getId());
    }

    protected Optional<VergeGrunnlagEntitet> getAktivtBehandlingsgrunnlag(Long behandlingId) {
        TypedQuery<VergeGrunnlagEntitet> query = entityManager.createQuery(
                "SELECT vg FROM VergeGrunnlag vg WHERE vg.behandling.id = :behandling_id AND vg.aktiv = 'J'", //$NON-NLS-1$
                VergeGrunnlagEntitet.class);

        query.setParameter("behandling_id", behandlingId); //$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(query);
    }
}
