package no.nav.foreldrepenger.domene.inngangsvilkaar;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

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
