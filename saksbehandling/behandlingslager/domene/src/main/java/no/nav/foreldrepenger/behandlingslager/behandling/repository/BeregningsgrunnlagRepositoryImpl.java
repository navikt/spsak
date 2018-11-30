package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BeregningsgrunnlagRepositoryImpl implements BeregningsgrunnlagRepository {
    private EntityManager entityManager;
    private BehandlingLåsRepository behandlingLåsRepository;

    BeregningsgrunnlagRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BeregningsgrunnlagRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, BehandlingLåsRepository behandlingLåsRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        Objects.requireNonNull(behandlingLåsRepository, "behandlingLåsRepository"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.behandlingLåsRepository = behandlingLåsRepository;
    }

    @Override
    public Beregningsgrunnlag hentAggregat(Behandling behandling) {
        return hentBeregningsgrunnlag(behandling)
            .orElseThrow(() -> new IllegalStateException("Mangler Beregningsgrunnlag for behandling " + behandling.getId()));
    }

    @Override
    public Optional<Beregningsgrunnlag> hentBeregningsgrunnlag(Behandling behandling) {
        return hentBeregningsgrunnlagGrunnlagEntitet(behandling).map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
    }

    @Override
    public Optional<Beregningsgrunnlag> hentBeregningsgrunnlag(Long beregningsgrunnlagId) {
        TypedQuery<Beregningsgrunnlag> query = entityManager.createQuery(
            "from Beregningsgrunnlag grunnlag " +
                "where grunnlag.id = :beregningsgrunnlagId", Beregningsgrunnlag.class); //$NON-NLS-1$
        query.setParameter("beregningsgrunnlagId", beregningsgrunnlagId); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    @Override
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentBeregningsgrunnlagGrunnlagEntitet(Behandling behandling) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
            "from BeregningsgrunnlagGrunnlagEntitet grunnlag " +
                "where grunnlag.behandling.id=:behandlingId " +
                "and grunnlag.aktiv = :aktivt", BeregningsgrunnlagGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandling.getId()); //$NON-NLS-1$
        query.setParameter("aktivt", true); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    @Override
    public Optional<BeregningsgrunnlagGrunnlagEntitet> hentSisteBeregningsgrunnlagGrunnlagEntitet(Behandling behandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        TypedQuery<BeregningsgrunnlagGrunnlagEntitet> query = entityManager.createQuery(
            "from BeregningsgrunnlagGrunnlagEntitet " +
                "where behandling.id=:behandlingId " +
                "and beregningsgrunnlagTilstand = :beregningsgrunnlagTilstand " +
                "order by opprettetTidspunkt desc", BeregningsgrunnlagGrunnlagEntitet.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandling.getId()); //$NON-NLS-1$
        query.setParameter("beregningsgrunnlagTilstand", beregningsgrunnlagTilstand); //$NON-NLS-1$
        return query.getResultList().stream().findFirst();
    }

    @Override
    public long lagre(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        final BehandlingLås lås = behandlingLåsRepository.taLås(behandling.getId());
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        Optional<Beregningsgrunnlag> eksisterendeBG = entitetOpt.map(BeregningsgrunnlagGrunnlagEntitet::getBeregningsgrunnlag);
        // er vi i ferd med å persistere eksisterende Beregningsgrunnlag på nytt?
        if (eksisterendeBG.isPresent() && eksisterendeBG.get().getId().equals(beregningsgrunnlag.getId())) {
            throw new IllegalStateException("Forsøker å lagre samme beregningsgrunnlag på nytt: " + beregningsgrunnlag.getId());
        }
        entitetOpt.ifPresent(this::deaktiverBeregningsgrunnlagGrunnlagEntitet);
        entityManager.persist(beregningsgrunnlag);
        opprettOgLagreBeregningsgrunnlagEntitet(behandling, beregningsgrunnlag, beregningsgrunnlagTilstand);
        verifiserBehandlingLås(lås);
        entityManager.flush();
        return beregningsgrunnlag.getId();
    }

    // sjekk lås og oppgrader til skriv
    protected void verifiserBehandlingLås(BehandlingLås lås) {
        behandlingLåsRepository.oppdaterLåsVersjon(lås);
    }

    @Override
    public void deaktiverBeregningsgrunnlagGrunnlagEntitet(Behandling behandling) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> entitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        entitetOpt.ifPresent(this::deaktiverBeregningsgrunnlagGrunnlagEntitet);
    }

    private void deaktiverBeregningsgrunnlagGrunnlagEntitet(BeregningsgrunnlagGrunnlagEntitet entitet) {
        setAktivOgLagre(entitet, false);
    }

    private void setAktivOgLagre(BeregningsgrunnlagGrunnlagEntitet entitet, boolean aktiv) {
        entitet.setAktiv(aktiv);
        entityManager.persist(entitet);
        entityManager.flush();
    }

    @Override
    public void reaktiverBeregningsgrunnlagGrunnlagEntitet(Behandling behandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Optional<BeregningsgrunnlagGrunnlagEntitet> aktivEntitetOpt = hentBeregningsgrunnlagGrunnlagEntitet(behandling);
        aktivEntitetOpt.ifPresent(this::deaktiverBeregningsgrunnlagGrunnlagEntitet);
        Optional<BeregningsgrunnlagGrunnlagEntitet> kontrollerFaktaEntitetOpt = hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, beregningsgrunnlagTilstand);
        kontrollerFaktaEntitetOpt.ifPresent(entitet -> setAktivOgLagre(entitet, true));
        entityManager.flush();
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        Optional<Beregningsgrunnlag> beregningsgrunnlag = hentBeregningsgrunnlag(gammelBehandling);
        beregningsgrunnlag.ifPresent(orig -> opprettOgLagreBeregningsgrunnlagEntitet(nyBehandling, orig, beregningsgrunnlagTilstand));
    }

    private Optional<Long> finnAktivBeregningId(Behandling behandling) {
        return hentBeregningsgrunnlagGrunnlagEntitet(behandling).map(BeregningsgrunnlagGrunnlagEntitet::getId);
    }

    //Denne metoden bør legges i Tjeneste
    @Override
    public EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling) {
        Optional<Long> funnetId = finnAktivBeregningId(behandling);
        return funnetId
            .map(id -> EndringsresultatSnapshot.medSnapshot(Beregningsgrunnlag.class, id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(Beregningsgrunnlag.class));
    }


    private void opprettOgLagreBeregningsgrunnlagEntitet(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        BeregningsgrunnlagGrunnlagEntitet entitet = new BeregningsgrunnlagGrunnlagEntitet(behandling, beregningsgrunnlag, beregningsgrunnlagTilstand);
        entityManager.persist(entitet);
    }
}
