package no.nav.foreldrepenger.behandling.revurdering.fp;

import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

class OppfyllerIkkeInngangsvilkårPåSkjæringstidspunkt {

    private static final Set<VilkårType> VILKÅR_SOM_MÅ_VÆRE_OPPFYLT;

    static {
        VILKÅR_SOM_MÅ_VÆRE_OPPFYLT = Set.of(VilkårType.OPPTJENINGSVILKÅRET, VilkårType.BEREGNINGSGRUNNLAGVILKÅR, VilkårType.MEDLEMSKAPSVILKÅRET);
    }

    private OppfyllerIkkeInngangsvilkårPåSkjæringstidspunkt() {
    }

    public static boolean vurder(Behandlingsresultat behandlingsresultat) {
        List<Vilkår> vilkårene = behandlingsresultat.getVilkårResultat().getVilkårene();

        return !vilkårene
            .stream()
            .filter(v -> VILKÅR_SOM_MÅ_VÆRE_OPPFYLT.contains(v.getVilkårType()))
            .map(Vilkår::getGjeldendeVilkårUtfall)
            .allMatch(VilkårUtfallType.OPPFYLT::equals);
    }

    public static Behandlingsresultat fastsett(Behandling revurdering, Behandlingsresultat behandlingsresultat) {
        return SettOpphørOgIkkeRett.fastsett(revurdering, behandlingsresultat);
    }
}
