package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

/**
 * Dette er et Repository for håndtering av alle persistente endringer i en søkers perioder for medlemskapvilkår
 * <p>
 * Merk: "standard" regler adoptert for "grunnlag" (ikke helt standard, ettersom vi her knytter
 * MedlemskapVilkårPeriodeGrunnlag til Vilkårsresultat i stedet for Behandling) - ett Grunnlag eies av ett
 * Vilkårsresultat. Et Aggregat (MedlemskapVilkårPeriodeGrunnlag-graf) har en selvstenig livssyklus og vil kopieres
 * ved hver endring.
 * Ved multiple endringer i et grunnlag for et MedlemskapVilkårPeriodeGrunnlag vil alltid kun et innslag i grunnlag
 * være aktiv for angitt Vilkårsresultat.
 */
@ApplicationScoped
public class MedlemskapVilkårPeriodeRepositoryImpl implements MedlemskapVilkårPeriodeRepository {

    private EntityManager entityManager;

    public MedlemskapVilkårPeriodeRepositoryImpl() {
        // FOR CDI
    }

    @Inject
    public MedlemskapVilkårPeriodeRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private EntityManager getEntityManager() {
        Objects.requireNonNull(this.entityManager, "entityManager ikke satt"); //$NON-NLS-1$
        return this.entityManager;
    }

    @Override
    public Optional<MedlemskapsvilkårPeriodeGrunnlag> hentAggregatHvisEksisterer(Behandling behandling) {
        Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> grunnlagEntitet = hentAktivtGrunnlag(behandling);
        return grunnlagEntitet.map(this::mapEntitetTilGrunnlag);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling eksisterendeBehandling, Behandling nyBehandling) {
        Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> eksisterendeGrunnlag = hentAktivtGrunnlag(eksisterendeBehandling);
        if (!eksisterendeGrunnlag.isPresent()) {
            return; // Intet å kopiere
        }
        MedlemskapVilkårPeriodeGrunnlagEntitet nyttGrunnlag = MedlemskapVilkårPeriodeGrunnlagEntitet.fra(eksisterendeGrunnlag, nyBehandling);
        EntityManager em = getEntityManager();
        em.persist(nyttGrunnlag);
        em.flush();
    }

    @Override
    public MedlemskapsvilkårPeriodeGrunnlag.Builder hentBuilderFor(Behandling behandling) {
        return opprettGrunnlagBuilderFor(behandling);
    }

    @Override
    public void lagreMedlemskapsvilkår(Behandling behandling, MedlemskapsvilkårPeriodeGrunnlag.Builder builder) {
        lagreOgFlush(behandling, builder.build());
    }

    @Override
    public VilkårUtfallType utledeVilkårStatus(Behandling behandling) {
        Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> medlemOpt = hentAktivtGrunnlag(behandling);

        if (medlemOpt.isPresent()) {
            MedlemskapsvilkårPeriodeEntitet medlemskapsvilkårPeriode = medlemOpt.get().getMedlemskapsvilkårPeriode();
            Set<MedlemskapsvilkårPerioder> perioder = medlemskapsvilkårPeriode.getPerioder();
            if (perioder
                .stream()
                .anyMatch(m -> VilkårUtfallType.IKKE_OPPFYLT.equals(m.getVilkårUtfall()))) {
                return VilkårUtfallType.IKKE_OPPFYLT;
            } else if (!perioder.isEmpty()) {
                return VilkårUtfallType.OPPFYLT;
            }
        }
        return VilkårUtfallType.IKKE_VURDERT;
    }

    private MedlemskapsvilkårPeriodeGrunnlag.Builder opprettGrunnlagBuilderFor(Behandling behandling) {
        Optional<MedlemskapsvilkårPeriodeGrunnlag> aggregat = hentAggregatHvisEksisterer(behandling);
        return MedlemskapsvilkårPeriodeGrunnlag.Builder.oppdatere(aggregat);
    }

    private MedlemskapsvilkårPeriodeGrunnlag mapEntitetTilGrunnlag(MedlemskapVilkårPeriodeGrunnlagEntitet entitet) {
        return MedlemskapsvilkårPeriodeGrunnlag.Builder.oppdatere(Optional.of(new MedlemskapsvilkårPeriodeGrunnlag()))
            .medmedlemskapsvilkårPeriode(entitet.getMedlemskapsvilkårPeriode())
            .build();
    }

    private Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> hentAktivtGrunnlag(Behandling behandling) {
        VilkårResultat vilkårResultat = Optional.ofNullable(behandling.getBehandlingsresultat())
            .map(Behandlingsresultat::getVilkårResultat)
            .orElse(null);
        if (vilkårResultat == null) {
            return Optional.empty();
        }
        TypedQuery<MedlemskapVilkårPeriodeGrunnlagEntitet> query = entityManager.createQuery("FROM MedlemskapVilkårPeriodeGrunnlag gr " +
            "WHERE gr.vilkårResultat.id = :vilkar_res_id " +
            "AND gr.aktiv = :aktivt", MedlemskapVilkårPeriodeGrunnlagEntitet.class);
        query.setParameter("vilkar_res_id", vilkårResultat.getId());
        query.setParameter("aktivt", true);
        return HibernateVerktøy.hentUniktResultat(query);
    }


    private void lagreOgFlush(Behandling behandling, MedlemskapsvilkårPeriodeGrunnlag nyttGrunnlag) {
        final Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> eksisterendeGrunnlag = hentAktivtGrunnlag(behandling);
        MedlemskapVilkårPeriodeGrunnlagEntitet nyGrunnlagEntitet = tilGrunnlagEntitet(behandling.getBehandlingsresultat().getVilkårResultat(), nyttGrunnlag);
        if (eksisterendeGrunnlag.isPresent()) {
            if (!erEndret(eksisterendeGrunnlag.get(), nyGrunnlagEntitet, behandling.getFagsakYtelseType(), true)) {
                return;
            }
            MedlemskapVilkårPeriodeGrunnlagEntitet eksisterendeGrunnlagEntitet = eksisterendeGrunnlag.get();
            eksisterendeGrunnlagEntitet.setAktiv(false);
            entityManager.persist(eksisterendeGrunnlagEntitet);
            entityManager.flush();
        }
        lagreGrunnlag(behandling.getBehandlingsresultat().getVilkårResultat(), nyttGrunnlag);
        entityManager.flush();
    }

    public boolean erEndret(MedlemskapVilkårPeriodeGrunnlagEntitet grunnlag1, MedlemskapVilkårPeriodeGrunnlagEntitet grunnlag2, FagsakYtelseType ytelseType, boolean onlyCheckTrackedFields) {
        DiffResult diff = new RegisterdataDiffsjekker(YtelseKode.valueOf(ytelseType.getKode()), onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
        return !diff.isEmpty();
    }


    private void lagreGrunnlag(VilkårResultat vilkårResultat, MedlemskapsvilkårPeriodeGrunnlag aggregat) {
        MedlemskapVilkårPeriodeGrunnlagEntitet grunnlag = tilGrunnlagEntitet(vilkårResultat, aggregat);

        entityManager.persist(grunnlag.getMedlemskapsvilkårPeriode());
        grunnlag.getMedlemskapsvilkårPeriode().getPerioder().forEach(periode -> {
                MedlemskapsvilkårPerioderEntitet periodeEntitet = (MedlemskapsvilkårPerioderEntitet) periode;
                periodeEntitet.setRot(grunnlag.getMedlemskapsvilkårPeriode());
                entityManager.persist(periode);
            }
        );
        entityManager.persist(grunnlag);
    }

    private MedlemskapVilkårPeriodeGrunnlagEntitet tilGrunnlagEntitet(VilkårResultat vilkårResultat, MedlemskapsvilkårPeriodeGrunnlag grunnlag) {
        MedlemskapVilkårPeriodeGrunnlagEntitet grunnlagEntitet = new MedlemskapVilkårPeriodeGrunnlagEntitet();
        grunnlagEntitet.setVilkårResultat(vilkårResultat);
        grunnlagEntitet.setMedlemskapsvilkårPeriode(grunnlag.getMedlemskapsvilkårPeriode());
        return grunnlagEntitet;
    }
}
