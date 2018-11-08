package no.nav.foreldrepenger.inngangsvilkaar.omsorgsovertakelse;

import static java.util.Collections.singletonList;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.inngangsvilkaar.Inngangsvilkår;
import no.nav.foreldrepenger.inngangsvilkaar.VilkårTypeRef;

@ApplicationScoped
@VilkårTypeRef(VilkårType.FP_VK_8)
public class InngangsvilkårEngangsstønadForeldreansvar1 implements Inngangsvilkår {

    public InngangsvilkårEngangsstønadForeldreansvar1() {
        // for CDI proxy
    }

    @Override
    public VilkårData vurderVilkår(Behandling behandling) {
        return new VilkårData(VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD, VilkårUtfallType.IKKE_VURDERT, 
                singletonList(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_2_LEDD));
    }
}
