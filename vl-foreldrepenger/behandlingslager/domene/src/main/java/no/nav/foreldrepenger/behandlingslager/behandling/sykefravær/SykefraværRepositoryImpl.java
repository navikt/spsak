package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.Sykefravær;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.Sykemelding;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.Sykemeldinger;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class SykefraværRepositoryImpl implements SykefraværRepository {

    private EntityManager entityManager;

    SykefraværRepositoryImpl() {
        // CDI
    }

    @Inject
    public SykefraværRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public SykemeldingerBuilder oppretBuilderForSykemeldinger(Long behandlingId) {
        return SykemeldingerBuilder.oppdater(getAktivtGrunnlag(behandlingId)
            .map(SykefraværGrunnlagEntitet::getSykemeldinger));
    }

    @Override
    public SykefraværBuilder oppretBuilderForSykefravær(Long behandlingId) {
        return SykefraværBuilder.oppdater(getAktivtGrunnlag(behandlingId)
            .map(SykefraværGrunnlagEntitet::getSykefravær));
    }

    @Override
    public void lagre(Behandling behandling, SykemeldingerBuilder builder) {
        SykefraværGrunnlagBuilder grunnlagBuilder = getSykefraværGrunnlagBuilder(behandling);

        grunnlagBuilder.medSykemeldinger(builder);

        lagreOgFlush(behandling, grunnlagBuilder);
    }

    @Override
    public void lagre(Behandling behandling, SykefraværBuilder builder) {
        SykefraværGrunnlagBuilder grunnlagBuilder = getSykefraværGrunnlagBuilder(behandling);

        grunnlagBuilder.medSykefravær(builder);

        lagreOgFlush(behandling, grunnlagBuilder);
    }

    private SykefraværGrunnlagBuilder getSykefraværGrunnlagBuilder(Behandling behandling) {
        return SykefraværGrunnlagBuilder.oppdater(getAktivtGrunnlag(behandling.getId()));
    }

    private void lagreOgFlush(Behandling behandling, SykefraværGrunnlagBuilder grunnlagBuilder) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$ //NOSONAR
        Objects.requireNonNull(grunnlagBuilder, "grunnlagBuilder"); //$NON-NLS-1$ //NOSONAR

        final Optional<SykefraværGrunnlagEntitet> aktivtGrunnlag = getAktivtGrunnlag(behandling.getId());

        final DiffEntity diffEntity = opprettDiffer();

        final SykefraværGrunnlag build = grunnlagBuilder.build();
        ((SykefraværGrunnlagEntitet) build).setBehandling(behandling);

        if (diffEntity.areDifferent(aktivtGrunnlag.orElse(null), build)) {
            aktivtGrunnlag.ifPresent(grunnlag -> {
                // setter gammelt grunnlag inaktiv. Viktig å gjøre før nye endringer siden vi kun
                // tillater ett aktivt grunnlag per behandling
                grunnlag.setAktiv(false);
                entityManager.persist(grunnlag);
                entityManager.flush();
            });
            Sykefravær sykefravær = build.getSykefravær();
            if (sykefravær != null) {
                entityManager.persist(sykefravær);
                for (SykefraværPeriode sykefraværPeriode : sykefravær.getPerioder()) {
                    entityManager.persist(sykefraværPeriode);
                }
            }
            Sykemeldinger sykemeldinger = build.getSykemeldinger();
            if (sykemeldinger != null) {
                entityManager.persist(sykemeldinger);
                for (Sykemelding sykemelding : sykemeldinger.getSykemeldinger()) {
                    entityManager.persist(sykemelding);
                }
            }
            entityManager.persist(build);
            entityManager.flush();
        }
    }

    private Optional<SykefraværGrunnlagEntitet> getAktivtGrunnlag(Long behandlingId) {
        TypedQuery<SykefraværGrunnlagEntitet> query = entityManager.createQuery(
            "SELECT sfg FROM SykefraværGrunnlagEntitet sfg WHERE sfg.behandling.id = :behandling_id AND sfg.aktiv = true", //$NON-NLS-1$
            SykefraværGrunnlagEntitet.class)
            .setParameter("behandling_id", behandlingId); //$NON-NLS-1$

        return HibernateVerktøy.hentUniktResultat(query);
    }

    private DiffEntity opprettDiffer() {
        TraverseEntityGraph traverser = TraverseEntityGraphFactory.build();
        return new DiffEntity(traverser);
    }

    @Override
    public Optional<SykefraværGrunnlag> hentHvisEksistererFor(Long behandlingId) {
        Optional<SykefraværGrunnlagEntitet> aktivtGrunnlag = getAktivtGrunnlag(behandlingId);
        if (aktivtGrunnlag.isPresent()) {
            return Optional.of(aktivtGrunnlag.get());
        }
        return Optional.empty();
    }

    @Override
    public SykefraværGrunnlag hentFor(Long behandlingId) {
        return getAktivtGrunnlag(behandlingId).orElseThrow(IllegalStateException::new);
    }
}
