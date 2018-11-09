package no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public class SkjermlenkeTjeneste {


    private SkjermlenkeTjeneste() {
    }

    /**
     * Returnerer skjermlenketype for eit aksjonspunkt. Inneheld logikk for spesialbehandling av aksjonspunkt som ikkje ligg på aksjonspunktdefinisjonen.
     */
    public static SkjermlenkeType finnSkjermlenkeType(AksjonspunktDefinisjon aksjonspunktDefinisjon, Behandling behandling) {
        if (AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE.equals(aksjonspunktDefinisjon) ||
            AksjonspunktDefinisjon.AVKLAR_OM_ANNEN_FORELDRE_HAR_MOTTATT_STØTTE.equals(aksjonspunktDefinisjon)) {
            return getSkjermlenkeTypeForMottattStotte(behandling);
        }
        if (AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE.equals(aksjonspunktDefinisjon) ){
            return getSkjermlenkeTypeForOmsorgsovertakelse(behandling);
        }
        return aksjonspunktDefinisjon.getSkjermlenkeType();
    }

    private static SkjermlenkeType getSkjermlenkeTypeForOmsorgsovertakelse(Behandling behandling) {
        FagsakYtelseType fagsakYtelseType = behandling.getFagsakYtelseType();
        return SkjermlenkeType.FAKTA_FOR_OMSORG;
    }

    private static SkjermlenkeType getSkjermlenkeTypeForMottattStotte(Behandling behandling) {
        VilkårType vilkårType = behandling.getVilkårTypeForRelasjonTilBarnet().orElse(null);
        if (VilkårType.FØDSELSVILKÅRET_MOR.equals(vilkårType) || VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR.equals(vilkårType)) {
            return SkjermlenkeType.PUNKT_FOR_FOEDSEL;
        } else if (VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD.equals(vilkårType) || VilkårType.ADOPSJONSVILKARET_FORELDREPENGER.equals(vilkårType)) {
            return SkjermlenkeType.PUNKT_FOR_ADOPSJON;
        } else if (VilkårType.OMSORGSVILKÅRET.equals(vilkårType)) {
            return SkjermlenkeType.PUNKT_FOR_OMSORG;
        } else if (VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD.equals(vilkårType) || VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD.equals(vilkårType)) {
            return SkjermlenkeType.PUNKT_FOR_FORELDREANSVAR;
        }
        return SkjermlenkeType.UDEFINERT;
    }
}
