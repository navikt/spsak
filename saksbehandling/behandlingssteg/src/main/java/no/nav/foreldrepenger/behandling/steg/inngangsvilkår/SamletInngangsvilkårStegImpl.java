package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;

import static java.util.Collections.emptyList;

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


// Steget sikrer at vilkårne blir vurdert samlet som inngangsvilkår
@BehandlingStegRef(kode = "VURDERSAMLET")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class SamletInngangsvilkårStegImpl extends InngangsvilkårStegImpl {

    private static List<VilkårType> STØTTEDE_VILKÅR = emptyList();

    @Inject
    public SamletInngangsvilkårStegImpl(BehandlingRepositoryProvider repositoryProvider, RegelOrkestrerer regelOrkestrerer) {
        super(repositoryProvider, regelOrkestrerer, BehandlingStegType.VURDER_SAMLET);
    }

    @Override
    public List<VilkårType> vilkårHåndtertAvSteg() {
        return STØTTEDE_VILKÅR;
    }
}
