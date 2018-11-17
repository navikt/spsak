package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

class OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt {

    private static final Set<VilkårType> VILKÅR_SOM_MÅ_VÆRE_OPPFYLT;

    private OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt() {
    }

    static {
        Set<VilkårType> set = new HashSet<>();
        set.add(VilkårType.FØDSELSVILKÅRET_MOR);
        set.add(VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR);
        set.add(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER);
        set.add(VilkårType.OPPTJENINGSVILKÅRET);
        set.add(VilkårType.SØKERSOPPLYSNINGSPLIKT);
        set.add(VilkårType.BEREGNINGSGRUNNLAGVILKÅR);
        set.add(VilkårType.MEDLEMSKAPSVILKÅRET);
        VILKÅR_SOM_MÅ_VÆRE_OPPFYLT = Collections.unmodifiableSet(set);
    }

    public static boolean vurder(Behandling revurdering) {
        List<Vilkår> vilkårene = revurdering.getBehandlingsresultat().getVilkårResultat().getVilkårene();

        return !vilkårene
            .stream()
            .filter(v -> VILKÅR_SOM_MÅ_VÆRE_OPPFYLT.contains(v.getVilkårType()))
            .map(Vilkår::getGjeldendeVilkårUtfall)
            .allMatch(VilkårUtfallType.OPPFYLT::equals);
    }

    public static Behandlingsresultat fastsett(Behandling revurdering) {
        return SettOpphørOgIkkeRett.fastsett(revurdering);
    }
}
