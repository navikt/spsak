package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;

@BehandlingStegRef(kode = "VURDERSFV")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class SøknadsfristVilkårStegImpl extends InngangsvilkårStegImpl {

    private static List<VilkårType> STØTTEDE_VILKÅR = List.of(
        VilkårType.SØKNADSFRISTVILKÅRET
    );

    @Inject
    public SøknadsfristVilkårStegImpl(BehandlingRepositoryProvider repositoryProvider, RegelOrkestrerer regelOrkestrerer) {
        super(repositoryProvider, regelOrkestrerer, BehandlingStegType.VURDER_SØKNADSFRISTVILKÅR);
    }

    @Override
    public List<VilkårType> vilkårHåndtertAvSteg() {
        return STØTTEDE_VILKÅR;
    }

}
