package no.nav.foreldrepenger.domene.inngangsvilkaar;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

/**
 * Denne angir implementasjon som skal brukes for en gitt {@link VilkårType} slik at {@link Vilkår} og
 * {@link VilkårResultat} kan fastsettes.
 *
 */
public interface InngangsvilkårTjeneste {

    /** Finn {@link Inngangsvilkår} for angitt {@link VilkårType}. Husk at denne må closes når du er ferdig med den. */
    Inngangsvilkår finnVilkår(VilkårType vilkårType);

    /** Overstyr gitt aksjonspunkt på Inngangsvilkår. */
    void overstyrAksjonspunkt(Long behandlingId, VilkårType vilkårType, VilkårUtfallType utfall, String avslagskode, BehandlingskontrollKontekst kontekst);

}
