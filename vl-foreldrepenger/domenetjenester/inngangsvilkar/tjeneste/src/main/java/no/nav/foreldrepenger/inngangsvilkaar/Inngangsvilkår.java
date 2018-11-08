package no.nav.foreldrepenger.inngangsvilkaar;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;

public interface Inngangsvilkår {

    /**
     * Vurder vilkår og returner utfall
     * 
     * @param behandling
     *            - med grunnlag som skal vurderes
     * @return {@link VilkårData} som beskriver utfall
     */
    VilkårData vurderVilkår(Behandling behandling);

}
