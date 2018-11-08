package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

/**
 * Dette er et Repository for håndtering av alle persistente endringer i en Medlemskap for søker.
 * <p>
 * Hent opp og lagre innhentende Medlemskap data, fra søknad, register (MEDL) eller som avklart av Saksbehandler.
 * Ved hver endring kopieres Medlemskap grafen (inklusiv oppgitt tilknytning og utenlandsopphold) som et felles
 * Aggregat (ref. Domain Driven Design - Aggregat pattern)
 * <p>
 * <p>
 * Merk: standard regler - et Grunnlag eies av en Behandling. Et Aggregat (Søkers Medlemskap graf) har en
 * selvstenig livssyklus og vil kopieres ved hver endring.
 * Ved multiple endringer i et grunnlat for en Behandling vil alltid kun et innslag i grunnlag være aktiv for angitt
 * Behandling.
 */
@ApplicationScoped
public class MedlemskapRepositoryImpl implements MedlemskapRepository {

    private EntityManager entityManager;
    private BehandlingLåsRepositoryImpl behandlingLåsRepository;

    public MedlemskapRepositoryImpl() {
        // FOR CDI
    }

    @Inject
    public MedlemskapRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
        this.behandlingLåsRepository = new BehandlingLåsRepositoryImpl(entityManager);
    }

    @Override
    public Optional<MedlemskapAggregat> hentMedlemskap(Behandling behandling) {
        Optional<MedlemskapBehandlingsgrunnlagEntitet> optGrunnlag = getAktivtBehandlingsgrunnlag(behandling);
        return hentMedlemskap(optGrunnlag);
    }

    @Override
    public Optional<MedlemskapAggregat> hentMedlemskap(Long behandlingId) {
        Optional<MedlemskapBehandlingsgrunnlagEntitet> optGrunnlag = getAktivtBehandlingsgrunnlag(behandlingId);
        return hentMedlemskap(optGrunnlag);
    }

    @Override
    public Optional<VurdertMedlemskap> hentVurdertMedlemskap(Behandling behandling) {
        Optional<MedlemskapAggregat> medlemskap = hentMedlemskap(behandling);
        if (medlemskap.isPresent()) {
            return medlemskap.get().getVurdertMedlemskap();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean erEndret(Long grunnlagId, Behandling behandling) {
        Objects.requireNonNull(behandling.getId(), "behandlingId"); //$NON-NLS-1$ //NOSONAR
        Long aktivGrunnlagId = getAktivtBehandlingsgrunnlag(behandling.getId())
            .map(it -> it.getId())
            .orElse(null);
        return Objects.equals(grunnlagId, aktivGrunnlagId);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling eksisterendeBehandling, Behandling nyBehandling) {
        final BehandlingLås nyLås = taLås(nyBehandling);
        Optional<MedlemskapBehandlingsgrunnlagEntitet> eksisterendeGrunnlag = getAktivtBehandlingsgrunnlag(eksisterendeBehandling);

        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = MedlemskapBehandlingsgrunnlagEntitet.fra(eksisterendeGrunnlag, nyBehandling);

        lagreOgFlush(Optional.empty(), nyttGrunnlag);
        oppdaterLås(nyLås);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandlingForRevurdering(Behandling eksisterendeBehandling, Behandling nyBehandling) {
        final BehandlingLås nyLås = taLås(nyBehandling);
        Optional<MedlemskapBehandlingsgrunnlagEntitet> eksisterendeGrunnlag = getAktivtBehandlingsgrunnlag(eksisterendeBehandling);

        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = MedlemskapBehandlingsgrunnlagEntitet.forRevurdering(eksisterendeGrunnlag,
                nyBehandling);

        lagreOgFlush(Optional.empty(), nyttGrunnlag);
        oppdaterLås(nyLås);
    }

    @Override
    public void lagreMedlemskapRegisterOpplysninger(Behandling behandling, Collection<RegistrertMedlemskapPerioder> registrertMedlemskap) {
        final BehandlingLås lås = taLås(behandling);
        Optional<MedlemskapBehandlingsgrunnlagEntitet> gr = getAktivtBehandlingsgrunnlag(behandling);
        MedlemskapRegistrertEntitet data = kopierOgLagreHvisEndret(gr, registrertMedlemskap);
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = MedlemskapBehandlingsgrunnlagEntitet.fra(gr, behandling, data);
        lagreOgFlush(gr, nyttGrunnlag);
        oppdaterLås(lås);
    }

    protected BehandlingLås taLås(Behandling behandling) {
        return behandlingLåsRepository.taLås(behandling.getId());
    }

    public void lagreMedlemskapRegistrert(MedlemskapRegistrertEntitet ny) {
        EntityManager em = getEntityManager();
        em.persist(ny);
        em.flush();
    }

    @Override
    public void lagreMedlemskapVurdering(Behandling behandling, VurdertMedlemskap vurdertMedlemskap) {
        final BehandlingLås lås = taLås(behandling);
        Optional<MedlemskapBehandlingsgrunnlagEntitet> gr = getAktivtBehandlingsgrunnlag(behandling);
        VurdertMedlemskapEntitet data = kopierOgLagreHvisEndret(gr, vurdertMedlemskap);
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = MedlemskapBehandlingsgrunnlagEntitet.fra(gr, behandling, data);
        lagreOgFlush(gr, nyttGrunnlag);
        oppdaterLås(lås);
    }

    public void lagreOgFlush(Optional<MedlemskapBehandlingsgrunnlagEntitet> tidligereGrunnlagOpt,
            MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag) {

        EntityManager em = getEntityManager();

        if (tidligereGrunnlagOpt.isPresent()) {
            MedlemskapBehandlingsgrunnlagEntitet tidligereGrunnlag = tidligereGrunnlagOpt.get();
            boolean erForskjellig = medlemskapAggregatDiffer(false).areDifferent(tidligereGrunnlag, nyttGrunnlag);
            if (erForskjellig) {
                tidligereGrunnlag.setAktiv(false);
                em.persist(tidligereGrunnlag);
                em.flush();
                em.persist(nyttGrunnlag);
            } else {
                return;
            }

        } else {
            em.persist(nyttGrunnlag);
        }

        em.flush();
    }

    @Override
    public void lagreOppgittTilkytning(Behandling behandling, OppgittTilknytning oppgittTilknytning) {
        final BehandlingLås lås = taLås(behandling);
        Optional<MedlemskapBehandlingsgrunnlagEntitet> gr = getAktivtBehandlingsgrunnlag(behandling);
        OppgittTilknytningEntitet data = kopierHvisEndretOgLagre(gr, oppgittTilknytning);
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = MedlemskapBehandlingsgrunnlagEntitet.fra(gr, behandling, data);
        lagreOgFlush(gr, nyttGrunnlag);
        oppdaterLås(lås);
    }

    @Override
    public void slettAvklarteMedlemskapsdata(Behandling behandling, BehandlingLås lås) {
        oppdaterLås(lås);
        Optional<MedlemskapBehandlingsgrunnlagEntitet> gr = getAktivtBehandlingsgrunnlag(behandling);
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = MedlemskapBehandlingsgrunnlagEntitet.fra(gr, behandling,
                (VurdertMedlemskapEntitet) null);
        lagreOgFlush(gr, nyttGrunnlag);
        getEntityManager().flush();
    }

    protected void oppdaterLås(BehandlingLås lås) {
        behandlingLåsRepository.oppdaterLåsVersjon(lås);
    }

    public void lagreVurdertMedlemskap(VurdertMedlemskapEntitet ny) {
        EntityManager em = getEntityManager();
        em.persist(ny);
        em.flush();
    }

    public void lagreOppgittTilknytning(OppgittTilknytningEntitet ny) {
        EntityManager em = getEntityManager();
        em.persist(ny);
        em.flush();
    }

    private EntityManager getEntityManager() {
        Objects.requireNonNull(this.entityManager, "entityManager ikke satt"); //$NON-NLS-1$
        return this.entityManager;
    }

    private Optional<MedlemskapAggregat> hentMedlemskap(Optional<MedlemskapBehandlingsgrunnlagEntitet> optGrunnlag) {
        if (optGrunnlag.isPresent()) {
            MedlemskapBehandlingsgrunnlagEntitet grunnlag = optGrunnlag.get();
            MedlemskapAggregat ma = grunnlag.tilAggregat();
            return Optional.of(ma);
        } else {
            return Optional.empty();
        }
    }

    private OppgittTilknytningEntitet kopierHvisEndretOgLagre(
            Optional<MedlemskapBehandlingsgrunnlagEntitet> gr, // NOSONAR
            OppgittTilknytning oppgittTilknytning) {

        OppgittTilknytningEntitet ny = new OppgittTilknytningEntitet(oppgittTilknytning);
        if (gr.isPresent()) {
            OppgittTilknytningEntitet eksisterende = gr.get().getOppgittTilknytning();
            boolean erForskjellig = medlemskapAggregatDiffer(false).areDifferent(eksisterende, ny);
            if (erForskjellig) {
                lagreOppgittTilknytning(ny);
                return ny;
            } else {
                return eksisterende;
            }
        } else {
            lagreOppgittTilknytning(ny);
            return ny;
        }
    }

    private MedlemskapRegistrertEntitet kopierOgLagreHvisEndret(Optional<MedlemskapBehandlingsgrunnlagEntitet> gr,
            Collection<RegistrertMedlemskapPerioder> registrertMedlemskapPerioder) {
        MedlemskapRegistrertEntitet ny = new MedlemskapRegistrertEntitet(registrertMedlemskapPerioder);

        if (gr.isPresent()) {
            MedlemskapRegistrertEntitet eksisterende = gr.get().getRegisterMedlemskap();
            boolean erForskjellig = medlemskapAggregatDiffer(false).areDifferent(eksisterende, ny);
            if (erForskjellig) {
                lagreMedlemskapRegistrert(ny);
                return ny;
            } else {
                return eksisterende;
            }
        } else {
            lagreMedlemskapRegistrert(ny);
            return ny;
        }
    }

    private VurdertMedlemskapEntitet kopierOgLagreHvisEndret(Optional<MedlemskapBehandlingsgrunnlagEntitet> gr,
            VurdertMedlemskap vurdertMedlemskap) {

        VurdertMedlemskapEntitet ny = new VurdertMedlemskapEntitet(vurdertMedlemskap);
        if (gr.isPresent()) {
            VurdertMedlemskapEntitet eksisterende = gr.get().getVurderingMedlemskapSkjæringstidspunktet();
            boolean erForskjellig = medlemskapAggregatDiffer(false).areDifferent(eksisterende, ny);
            if (erForskjellig) {
                lagreVurdertMedlemskap(ny);
                return ny;
            } else {
                return eksisterende;
            }
        } else {
            lagreVurdertMedlemskap(ny);
            return ny;
        }

    }

    private VurdertMedlemskapPeriodeEntitet kopierOgLagreHvisEndret(Optional<MedlemskapBehandlingsgrunnlagEntitet> gr, VurdertMedlemskapPeriode løpendeMedlemskap) {
        VurdertMedlemskapPeriodeEntitet ny = new VurdertMedlemskapPeriodeEntitet(løpendeMedlemskap);

        if (gr.isPresent()) {
            VurdertMedlemskapPeriodeEntitet eksisterende = gr.get().getVurderingLøpendeMedlemskap();
            boolean erForskjellig = medlemskapAggregatDiffer(false).areDifferent(eksisterende, ny);
            if (erForskjellig) {
                lagreVurdertLøpendeMedlemskap(ny);
                return ny;
            } else {
                return eksisterende;
            }
        } else {
            lagreVurdertLøpendeMedlemskap(ny);
            return ny;
        }
    }

    private void lagreVurdertLøpendeMedlemskap(VurdertMedlemskapPeriodeEntitet ny) {
        EntityManager entityManager = getEntityManager();
        entityManager.persist(ny);
        ny.getPerioder().forEach(vurdertLøpendeMedlemskap -> {
            VurdertLøpendeMedlemskapEntitet entitet = (VurdertLøpendeMedlemskapEntitet) vurdertLøpendeMedlemskap;
            entityManager.persist(entitet);
        });
    }

    private DiffEntity medlemskapAggregatDiffer(boolean medOnlyCheckTrackedFields) {
        TraverseEntityGraph traverser = TraverseEntityGraphFactory.build(medOnlyCheckTrackedFields);
        return new DiffEntity(traverser);
    }

    protected Optional<MedlemskapBehandlingsgrunnlagEntitet> getAktivtBehandlingsgrunnlag(Behandling behandling) {
        return getAktivtBehandlingsgrunnlag(behandling.getId());
    }

    protected Optional<MedlemskapBehandlingsgrunnlagEntitet> getAktivtBehandlingsgrunnlag(Long behandlingId) {
        TypedQuery<MedlemskapBehandlingsgrunnlagEntitet> query = getEntityManager().createQuery(
                "SELECT mbg FROM MedlemskapBehandlingsgrunnlag mbg WHERE mbg.behandling.id = :behandling_id AND mbg.aktiv = 'J'", //$NON-NLS-1$
                MedlemskapBehandlingsgrunnlagEntitet.class)
                .setParameter("behandling_id", behandlingId); //$NON-NLS-1$

        return HibernateVerktøy.hentUniktResultat(query);
    }

    protected Optional<MedlemskapBehandlingsgrunnlagEntitet> getInitilVersjonAvBehandlingsgrunnlag(Long behandlingId) {
        // må også sortere på id da opprettetTidspunkt kun er til nærmeste millisekund og ikke satt fra db.
        TypedQuery<MedlemskapBehandlingsgrunnlagEntitet> query = getEntityManager().createQuery(
                "SELECT mbg FROM MedlemskapBehandlingsgrunnlag mbg WHERE mbg.behandling.id = :behandling_id ORDER BY mbg.opprettetTidspunkt, mbg.id", //$NON-NLS-1$
                MedlemskapBehandlingsgrunnlagEntitet.class)
                .setParameter("behandling_id", behandlingId); //$NON-NLS-1$

        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<MedlemskapAggregat> hentFørsteVersjonAvMedlemskap(Behandling behandling) {
        Optional<MedlemskapBehandlingsgrunnlagEntitet> optGrunnlag = getInitilVersjonAvBehandlingsgrunnlag(behandling.getId());
        return hentMedlemskap(optGrunnlag);
    }

    @Override
    public Optional<Long> hentIdPåAktivMedlemskap(Behandling behandling) {
        return getAktivtBehandlingsgrunnlag(behandling)
            .map(MedlemskapBehandlingsgrunnlagEntitet::getId);
    }

    @Override
    public MedlemskapAggregat hentMedlemskapPåId(Long aggregatId) {
        Optional<MedlemskapBehandlingsgrunnlagEntitet> optGrunnlag = getVersjonAvMedlemskapGrunnlagPåId(
            aggregatId);
        return optGrunnlag.isPresent() ? optGrunnlag.get().tilAggregat() : null;
    }

    @Override
    public DiffResult diffResultat(Long grunnlagId1, Long grunnlagId2, FagsakYtelseType ytelseType, boolean onlyCheckTrackedFields) {
        MedlemskapBehandlingsgrunnlagEntitet grunnlag1 = getVersjonAvMedlemskapGrunnlagPåId(grunnlagId1)
            .orElseThrow(() -> new IllegalStateException("id1 ikke kjent"));
        MedlemskapBehandlingsgrunnlagEntitet grunnlag2 = getVersjonAvMedlemskapGrunnlagPåId(grunnlagId2)
            .orElseThrow(() -> new IllegalStateException("id2 ikke kjent"));
        return new RegisterdataDiffsjekker(YtelseKode.valueOf(ytelseType.getKode()), onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    @Override
    public void lagreLøpendeMedlemskapVurdering(Behandling behandling, VurdertMedlemskapPeriode løpendeMedlemskap) {
        final BehandlingLås lås = taLås(behandling);
        Optional<MedlemskapBehandlingsgrunnlagEntitet> gr = getAktivtBehandlingsgrunnlag(behandling);
        VurdertMedlemskapPeriodeEntitet data = kopierOgLagreHvisEndret(gr, løpendeMedlemskap);
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = MedlemskapBehandlingsgrunnlagEntitet.fra(gr, behandling, data);
        lagreOgFlush(gr, nyttGrunnlag);
        oppdaterLås(lås);
    }

    private Optional<MedlemskapBehandlingsgrunnlagEntitet> getVersjonAvMedlemskapGrunnlagPåId(Long aggregatId) {
        TypedQuery<MedlemskapBehandlingsgrunnlagEntitet> query = getEntityManager().createQuery(
            "SELECT mbg FROM MedlemskapBehandlingsgrunnlag mbg WHERE mbg.id = :aggregatId", //$NON-NLS-1$
            MedlemskapBehandlingsgrunnlagEntitet.class)
            .setParameter("aggregatId", aggregatId);
        return query.getResultList().stream().findFirst();
    }
}
