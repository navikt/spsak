package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
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
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.util.Tuple;

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
    public Optional<MedlemskapVilkårPeriodeGrunnlag> hentAggregatHvisEksisterer(Behandling behandling) {
        Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> grunnlagEntitet = hentAktivtGrunnlag(behandling);
        if (grunnlagEntitet.isPresent()) {
            return Optional.of(grunnlagEntitet.get());
        }
        return Optional.empty();
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
    public MedlemskapVilkårPeriodeGrunnlagEntitet.Builder hentBuilderFor(Behandling behandling) {
        return opprettGrunnlagBuilderFor(behandling);
    }

    @Override
    public void lagreMedlemskapsvilkår(Behandling behandling, MedlemskapVilkårPeriodeGrunnlagEntitet.Builder builder) {
        lagreOgFlush(behandling, builder.build());
    }

    @Override
    public Tuple<VilkårUtfallType, VilkårUtfallMerknad> utledeVilkårStatus(Behandling behandling) {
        Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> medlemOpt = hentAktivtGrunnlag(behandling);
        if (medlemOpt.isPresent()) {
            MedlemskapsvilkårPeriodeEntitet medlemskapsvilkårPeriode = medlemOpt.get().getMedlemskapsvilkårPeriode();
            Set<MedlemskapsvilkårPerioder> perioder = medlemskapsvilkårPeriode.getPerioder();
            Optional<MedlemskapsvilkårPerioder> periodeOpt = perioder
                .stream()
                .filter(m -> VilkårUtfallType.IKKE_OPPFYLT.equals(m.getVilkårUtfall()))
                .findFirst();
            if (periodeOpt.isPresent()) {
                return new Tuple<>(VilkårUtfallType.IKKE_OPPFYLT, periodeOpt.get().getVilkårUtfallMerknad());
            } else if (!perioder.isEmpty()) {
                return new Tuple<>(VilkårUtfallType.OPPFYLT, VilkårUtfallMerknad.UDEFINERT);
            }
        }
        return new Tuple<>(VilkårUtfallType.IKKE_VURDERT, VilkårUtfallMerknad.UDEFINERT);
    }

    @Override
    public Optional<LocalDate> hentOpphørsdatoHvisEksisterer(Behandling behandling) {
        Optional<MedlemskapsvilkårPeriodeEntitet> periodeEntitet = hentAktivtGrunnlag(behandling)
            .map(MedlemskapVilkårPeriodeGrunnlagEntitet::getMedlemskapsvilkårPeriode);

        //tar hensyn til overstrying av vilkåret
        if (periodeEntitet.isPresent()) {
            MedlemskapsvilkårPeriodeEntitet entitet = periodeEntitet.get();
        }
        return periodeEntitet
            .map(MedlemskapsvilkårPeriodeEntitet::getPerioder)
            .flatMap(perioder -> perioder.stream().filter(p -> VilkårUtfallType.IKKE_OPPFYLT.equals(p.getVilkårUtfall()))
                .map(MedlemskapsvilkårPerioder::getVurderingsdato).findFirst());
    }

    private MedlemskapVilkårPeriodeGrunnlagEntitet.Builder opprettGrunnlagBuilderFor(Behandling behandling) {
        Optional<MedlemskapVilkårPeriodeGrunnlagEntitet> aggregat = hentAktivtGrunnlag(behandling);
        if (aggregat.isPresent()) {
            return MedlemskapVilkårPeriodeGrunnlagEntitet.Builder.oppdatere(Optional.of(new MedlemskapVilkårPeriodeGrunnlagEntitet(aggregat.get())));
        }
        return MedlemskapVilkårPeriodeGrunnlagEntitet.Builder.oppdatere(Optional.empty());
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


    private void lagreOgFlush(Behandling behandling, MedlemskapVilkårPeriodeGrunnlag nyttGrunnlag) {
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


    private void lagreGrunnlag(VilkårResultat vilkårResultat, MedlemskapVilkårPeriodeGrunnlag aggregat) {
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

    private MedlemskapVilkårPeriodeGrunnlagEntitet tilGrunnlagEntitet(VilkårResultat vilkårResultat, MedlemskapVilkårPeriodeGrunnlag grunnlag) {
        MedlemskapVilkårPeriodeGrunnlagEntitet grunnlagEntitet = new MedlemskapVilkårPeriodeGrunnlagEntitet();
        grunnlagEntitet.setVilkårResultat(vilkårResultat);
        grunnlagEntitet.setMedlemskapsvilkårPeriode((MedlemskapsvilkårPeriodeEntitet) grunnlag.getMedlemskapsvilkårPeriode());
        return grunnlagEntitet;
    }
}
