package no.nav.foreldrepenger.domene.familiehendelse.omsorg.impl;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

// TODO: Bør sannsynligvis ikke ligge her ..
public final class OmsorgsvilkårKonfigurasjon {
    private OmsorgsvilkårKonfigurasjon() {
    }

    public static Set<VilkårType> getOmsorgsovertakelseVilkår() {
        Set<VilkårType> set = new HashSet<>();
        set.addAll(asList(
            VilkårType.OMSORGSVILKÅRET,
            VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD,
            VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD));
        return set;
    }

    public static Set<AksjonspunktDefinisjon> getOmsorgsovertakelseAksjonspunkter() {
        Set<AksjonspunktDefinisjon> set = new HashSet<>();
        set.addAll(asList(
            AksjonspunktDefinisjon.MANUELL_VURDERING_AV_OMSORGSVILKÅRET,
            AksjonspunktDefinisjon.MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_2_LEDD,
            AksjonspunktDefinisjon.MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_4_LEDD));
        return set;
    }
}
