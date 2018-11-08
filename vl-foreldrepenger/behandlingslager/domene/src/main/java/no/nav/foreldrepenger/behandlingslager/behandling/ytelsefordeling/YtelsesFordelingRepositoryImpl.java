package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class YtelsesFordelingRepositoryImpl implements YtelsesFordelingRepository {

    private EntityManager entityManager;

    public YtelsesFordelingRepositoryImpl() {
        // CDI
    }

    @Inject
    public YtelsesFordelingRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    @Override
    public YtelseFordelingAggregat hentAggregat(Behandling behandling) {
        final Optional<YtelseFordelingGrunnlagEntitet> ytelseFordelingGrunnlagEntitet = hentAktivtGrunnlag(behandling);
        if (ytelseFordelingGrunnlagEntitet.isPresent()) {
            return mapEntitetTilAggregat(ytelseFordelingGrunnlagEntitet.get());
        }
        throw YtelseFordelingFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(behandling.getId()).toException();
    }

    @Override
    public Optional<YtelseFordelingGrunnlagEntitet> hentYtelseFordelingPåId(Long grunnlagId) {
        return getVersjonAvYtelsesFordelingPåId(grunnlagId);
    }

    @Override
    public YtelseFordelingAggregat hentYtelsesFordelingPåId(Long aggregatId) {
        Optional<YtelseFordelingGrunnlagEntitet> optGrunnlag = getVersjonAvYtelsesFordelingPåId(
            aggregatId);
        if (optGrunnlag.isPresent()) {
            return mapEntitetTilAggregat(optGrunnlag.get());
        }
        return new YtelseFordelingAggregat();
    }

    private YtelseFordelingAggregat mapEntitetTilAggregat(YtelseFordelingGrunnlagEntitet ytelseFordelingGrunnlagEntitet) {
        return YtelseFordelingAggregat.Builder.oppdatere(Optional.of(new YtelseFordelingAggregat()))
            .medOppgittDekningsgrad(ytelseFordelingGrunnlagEntitet.getDekningsgrad())
            .medOppgittRettighet(ytelseFordelingGrunnlagEntitet.getOppgittRettighet())
            .medPerioderUtenOmsorg(ytelseFordelingGrunnlagEntitet.getPerioderUtenOmsorg())
            .medPerioderAleneOmsorg(ytelseFordelingGrunnlagEntitet.getPerioderAleneOmsorgEntitet())
            .medOppgittFordeling(ytelseFordelingGrunnlagEntitet.getOppgittFordeling())
            .medOverstyrtFordeling(ytelseFordelingGrunnlagEntitet.getOverstyrtFordeling())
            .medPerioderUttakDokumentasjon(ytelseFordelingGrunnlagEntitet.getPerioderUttakDokumentasjon())
            .medAvklarteDatoer(ytelseFordelingGrunnlagEntitet.getAvklarteUttakDatoer())
            .build();
    }

    @Override
    public Optional<YtelseFordelingAggregat> hentAggregatHvisEksisterer(Behandling behandling) {
        final Optional<YtelseFordelingGrunnlagEntitet> entitet = hentAktivtGrunnlag(behandling);
        return entitet.map(this::mapEntitetTilAggregat);
    }

    @Override
    public void lagre(Behandling behandling, OppgittRettighet oppgittRettighet) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(oppgittRettighet, "oppgittRettighet");
        final YtelseFordelingAggregat.Builder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medOppgittRettighet(oppgittRettighet);

        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void lagre(Behandling behandling, OppgittFordeling oppgittPerioder) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(oppgittPerioder, "oppgittPerioder");
        final YtelseFordelingAggregat.Builder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medOppgittFordeling(oppgittPerioder);
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void lagreOverstyrtFordeling(Behandling behandling, OppgittFordeling oppgittPerioder, PerioderUttakDokumentasjonEntitet perioderUttakDokumentasjon) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(oppgittPerioder, "oppgittPerioder");
        final YtelseFordelingAggregat.Builder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medOverstyrtFordeling(oppgittPerioder);
        aggregatBuilder.medPerioderUttakDokumentasjon(perioderUttakDokumentasjon);
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void lagreOverstyrtFordeling(Behandling behandling, OppgittFordeling oppgittPerioder) {
        lagreOverstyrtFordeling(behandling, oppgittPerioder, null);
    }

    @Override
    public void lagre(Behandling behandling, OppgittDekningsgrad oppgittDekningsgrad) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(oppgittDekningsgrad, "oppgittDekningsgrad");
        final YtelseFordelingAggregat.Builder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medOppgittDekningsgrad(oppgittDekningsgrad);
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void lagre(Behandling behandling, PerioderUtenOmsorg perioderUtenOmsorg) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(perioderUtenOmsorg, "perioderUtenOmsorg");
        final YtelseFordelingAggregat.Builder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medPerioderUtenOmsorg(perioderUtenOmsorg);
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void lagre(Behandling behandling, PerioderAleneOmsorg perioderAleneOmsorg) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(perioderAleneOmsorg, "perioderAleneOmsorg");
        final YtelseFordelingAggregat.Builder aggregatBuilder = opprettAggregatBuilderFor(behandling);
        aggregatBuilder.medPerioderAleneOmsorg(perioderAleneOmsorg);
        lagreOgFlush(behandling, aggregatBuilder.build());
    }

    @Override
    public void lagre(Behandling behandling, AvklarteUttakDatoer avklarteUttakDatoer) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(avklarteUttakDatoer, "avklarteUttakDatoerEntitet");
        if (((AvklarteUttakDatoerEntitet) avklarteUttakDatoer).harVerdier()) {
            final YtelseFordelingAggregat.Builder aggregatBuilder = opprettAggregatBuilderFor(behandling);
            aggregatBuilder.medAvklarteDatoer(avklarteUttakDatoer);
            lagreOgFlush(behandling, aggregatBuilder.build());
        }
    }

    private Optional<YtelseFordelingGrunnlagEntitet> hentAktivtGrunnlag(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); // NOSONAR $NON-NLS-1$
        final TypedQuery<YtelseFordelingGrunnlagEntitet> query = entityManager.createQuery("FROM YtelseFordelingGrunnlag gr " +
            "WHERE gr.behandling.id = :behandlingId " +
            "AND gr.aktiv = :aktivt", YtelseFordelingGrunnlagEntitet.class);
        query.setParameter("behandlingId", behandling.getId());
        query.setParameter("aktivt", true);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    private void lagreOgFlush(Behandling behandling, YtelseFordelingAggregat aggregat) {
        final Optional<YtelseFordelingGrunnlagEntitet> eksisterendeGrunnlag = hentAktivtGrunnlag(behandling);
        YtelseFordelingGrunnlagEntitet nyGrunnlagEntitet = mapTilGrunnlagEntitet(behandling, aggregat);
        if (eksisterendeGrunnlag.isPresent()) {
            final YtelseFordelingGrunnlagEntitet eksisterendeGrunnlag1 = eksisterendeGrunnlag.get();
            eksisterendeGrunnlag1.setAktiv(false);
            entityManager.persist(eksisterendeGrunnlag1);
            entityManager.flush();
        }
        lagreGrunnlag(nyGrunnlagEntitet);
        entityManager.flush();
    }

    private void lagreGrunnlag(YtelseFordelingGrunnlagEntitet grunnlag) {

        if (grunnlag.getDekningsgrad() != null) {
            entityManager.persist(grunnlag.getDekningsgrad());
        }
        if (grunnlag.getOppgittRettighet() != null) {
            entityManager.persist(grunnlag.getOppgittRettighet());
        }
        if (grunnlag.getOppgittFordeling() != null) {
            entityManager.persist(grunnlag.getOppgittFordeling());
            lagrePeriode(grunnlag.getOppgittFordeling().getOppgittePerioder());
        }
        if (grunnlag.getOverstyrtFordeling() != null) {
            entityManager.persist(grunnlag.getOverstyrtFordeling());
            lagrePeriode(grunnlag.getOverstyrtFordeling().getOppgittePerioder());
        }
        if (grunnlag.getAvklarteUttakDatoer() != null) {
            entityManager.persist(grunnlag.getAvklarteUttakDatoer());
        }

        lagrePerioderAleneOmsorg(grunnlag);
        lagrePerioderUtenOmsorg(grunnlag);
        lagrePerioderUttakDokumentasjon(grunnlag);

        entityManager.persist(grunnlag);
    }

    private YtelseFordelingGrunnlagEntitet mapTilGrunnlagEntitet(Behandling behandling, YtelseFordelingAggregat aggregat) {
        final YtelseFordelingGrunnlagEntitet grunnlag = new YtelseFordelingGrunnlagEntitet();
        grunnlag.setBehandling(behandling);
        grunnlag.setDekningsgrad(aggregat.getOppgittDekningsgrad());
        grunnlag.setOppgittRettighet(aggregat.getOppgittRettighet());
        grunnlag.setOppgittFordeling(aggregat.getOppgittFordeling());
        aggregat.getPerioderUttakDokumentasjon().ifPresent(grunnlag::setPerioderUttakDokumentasjon);
        aggregat.getPerioderUtenOmsorg().ifPresent(grunnlag::setPerioderUtenOmsorg);
        aggregat.getPerioderAleneOmsorg().ifPresent(grunnlag::setPerioderAleneOmsorg);
        aggregat.getOverstyrtFordeling().ifPresent(grunnlag::setOverstyrtFordeling);
        aggregat.getAvklarteDatoer().ifPresent(grunnlag::setAvklarteUttakDatoerEntitet);
        return grunnlag;
    }

    private void lagrePerioderUttakDokumentasjon(YtelseFordelingGrunnlagEntitet grunnlag) {
        if (grunnlag.getPerioderUttakDokumentasjon() != null) {
            entityManager.persist(grunnlag.getPerioderUttakDokumentasjon());
            for (PeriodeUttakDokumentasjon periode : grunnlag.getPerioderUttakDokumentasjon().getPerioder()) {
                entityManager.persist(periode);
            }
        }
    }

    private void lagrePerioderUtenOmsorg(YtelseFordelingGrunnlagEntitet grunnlag) {
        if (grunnlag.getPerioderUtenOmsorg() != null) {
            entityManager.persist(grunnlag.getPerioderUtenOmsorg());
            for (PeriodeUtenOmsorg periode : grunnlag.getPerioderUtenOmsorg().getPerioder()) {
                entityManager.persist(periode);
            }
        }
    }

    private void lagrePerioderAleneOmsorg(YtelseFordelingGrunnlagEntitet grunnlag) {
        if (grunnlag.getPerioderAleneOmsorgEntitet() != null) {
            entityManager.persist(grunnlag.getPerioderAleneOmsorgEntitet());
            for (PeriodeAleneOmsorg periode : grunnlag.getPerioderAleneOmsorgEntitet().getPerioder()) {
                entityManager.persist(periode);
            }
        }
    }

    private void lagrePeriode(List<OppgittPeriode> perioder) {
        for (OppgittPeriode oppgittPeriode : perioder) {
            entityManager.persist(oppgittPeriode);
        }
    }

    private YtelseFordelingAggregat.Builder opprettAggregatBuilderFor(Behandling behandling) {
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = hentAggregatHvisEksisterer(behandling);
        return YtelseFordelingAggregat.Builder.oppdatere(ytelseFordelingAggregat);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling) {
        Optional<YtelseFordelingAggregat> origAggregat = hentAggregatHvisEksisterer(gammelBehandling);
        origAggregat.ifPresent(orig -> lagreOgFlush(nyBehandling, orig));
    }

    @Override
    public boolean erEndring(Behandling eksisterendeBehandling, Behandling nyBehandling) {
        Optional<YtelseFordelingGrunnlagEntitet> eksisterendeAggregat = hentAktivtGrunnlag(eksisterendeBehandling);
        Optional<YtelseFordelingGrunnlagEntitet> nyttAggregat = hentAktivtGrunnlag(nyBehandling);

        return erEndring(eksisterendeAggregat, nyttAggregat);
    }

    @Override
    public boolean erEndret(Long grunnlagId, Behandling behandling) {
        Objects.requireNonNull(behandling.getId(), "behandlingId"); //$NON-NLS-1$ //NOSONAR
        Long aktivGrunnlagId = hentAktivtGrunnlag(behandling)
            .map(it -> it.getId())
            .orElse(null);
        return Objects.equals(grunnlagId, aktivGrunnlagId);
    }

    private boolean erEndring(Optional<YtelseFordelingGrunnlagEntitet> eksisterende, Optional<YtelseFordelingGrunnlagEntitet> nytt) {
        if (!eksisterende.isPresent() && !nytt.isPresent()) {
            return false;
        }
        if (eksisterende.isPresent() && !nytt.isPresent()) {
            return true;
        }
        if (!eksisterende.isPresent() && nytt.isPresent()) { // NOSONAR - "redundant" her er false pos.
            return true;
        }

        TraverseEntityGraph traverser = TraverseEntityGraphFactory.build(true);

        DiffResult diff = new DiffEntity(traverser)
            .diff(eksisterende.get(), nytt.get());

        return !diff.isEmpty();
    }

    @Override
    public Optional<Long> hentIdPåAktivYtelsesFordeling(Behandling behandling) {
        return hentAktivtGrunnlag(behandling)
            .map(YtelseFordelingGrunnlagEntitet::getId);
    }

    @Override
    public void tilbakestillOverstyringOgDokumentasjonsperioder(Behandling behandling) {
        YtelseFordelingAggregat ytelseFordeling = opprettAggregatBuilderFor(behandling)
            .medPerioderUttakDokumentasjon(null)
            .medOverstyrtFordeling(null)
            .build();
        lagreOgFlush(behandling, ytelseFordeling);
    }

    @Override
    public void tilbakestillAvklarteDatoer(Behandling behandling) {
        YtelseFordelingAggregat ytelseFordeling = opprettAggregatBuilderFor(behandling)
            .medAvklarteDatoer(null)
            .build();
        lagreOgFlush(behandling, ytelseFordeling);
    }

    @Override
    public DiffResult diffResultat(Long grunnlagId1, Long grunnlagId2, FagsakYtelseType ytelseType, boolean onlyCheckTrackedFields) {
        YtelseFordelingGrunnlagEntitet grunnlag1 = hentYtelseFordelingPåId(grunnlagId1)
            .orElseThrow(() -> new IllegalStateException("GrunnlagId1 må være oppgitt"));
        YtelseFordelingGrunnlagEntitet grunnlag2 = hentYtelseFordelingPåId(grunnlagId2)
            .orElseThrow(() -> new IllegalStateException("GrunnlagId2 må være oppgitt"));
        return new RegisterdataDiffsjekker(YtelseKode.valueOf(ytelseType.getKode()), onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    private Optional<YtelseFordelingGrunnlagEntitet> getVersjonAvYtelsesFordelingPåId(
        Long aggregatId) {
        Objects.requireNonNull(aggregatId, "aggregatId"); // NOSONAR $NON-NLS-1$
        final TypedQuery<YtelseFordelingGrunnlagEntitet> query = entityManager.createQuery("FROM YtelseFordelingGrunnlag gr " +
            "WHERE gr.id = :aggregatId ", YtelseFordelingGrunnlagEntitet.class);
        query.setParameter("aggregatId", aggregatId);
        return HibernateVerktøy.hentUniktResultat(query);
    }
}
