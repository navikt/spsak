package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;

import static java.util.Collections.singletonList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapsvilkårPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapsvilkårPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelResultat;

@BehandlingStegRef(kode = "VURDERMV")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class VurderMedlemskapvilkårStegFPImpl extends InngangsvilkårStegImpl {

    private static List<VilkårType> STØTTEDE_VILKÅR = singletonList(
        VilkårType.MEDLEMSKAPSVILKÅRET
    );

    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    @Inject
    public VurderMedlemskapvilkårStegFPImpl(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider, RegelOrkestrerer regelOrkestrerer, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        super(repositoryProvider, regelOrkestrerer, BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);
        this.medlemskapVilkårPeriodeRepository = resultatRepositoryProvider.getMedlemskapVilkårPeriodeRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public List<VilkårType> vilkårHåndtertAvSteg() {
        return STØTTEDE_VILKÅR;
    }

    @Override
    protected void utførtRegler(BehandlingskontrollKontekst kontekst, Behandling behandling, RegelResultat regelResultat) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);

        Optional<Vilkår> medlemskapsvilkåret = behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene().stream().filter(v -> VilkårType.MEDLEMSKAPSVILKÅRET.equals(v.getVilkårType())).findFirst();
        VilkårUtfallType utfall = medlemskapsvilkåret.orElseThrow(() -> new IllegalStateException("Finner ikke medlemskapsvikåret.")).getGjeldendeVilkårUtfall();

        MedlemskapVilkårPeriodeGrunnlagEntitet.Builder builder = medlemskapVilkårPeriodeRepository.hentBuilderFor(behandling.getBehandlingsresultat());
        MedlemskapsvilkårPeriodeEntitet.Builder periodeBuilder = builder.getPeriodeBuilder();
        MedlemskapsvilkårPerioderEntitet.Builder periode = periodeBuilder.getBuilderForVurderingsdato(skjæringstidspunkt);
        periode.medVilkårUtfall(utfall);
        periodeBuilder.leggTil(periode);
        builder.medMedlemskapsvilkårPeriode(periodeBuilder);
        medlemskapVilkårPeriodeRepository.lagreMedlemskapsvilkår(behandling, builder);
    }
}
