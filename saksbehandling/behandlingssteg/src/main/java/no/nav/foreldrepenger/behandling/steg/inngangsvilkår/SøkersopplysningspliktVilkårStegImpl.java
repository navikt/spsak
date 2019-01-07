package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;

@BehandlingStegRef(kode = "VURDEROP")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class SøkersopplysningspliktVilkårStegImpl extends InngangsvilkårStegImpl {

    private static List<VilkårType> STØTTEDE_VILKÅR = List.of(
        VilkårType.SØKERSOPPLYSNINGSPLIKT
    );

    @Inject
    public SøkersopplysningspliktVilkårStegImpl(GrunnlagRepositoryProvider repositoryProvider, RegelOrkestrerer regelOrkestrerer) {
        super(repositoryProvider, regelOrkestrerer, BehandlingStegType.KONTROLLERER_SØKERS_OPPLYSNINGSPLIKT);
    }

    @Override
    public List<VilkårType> vilkårHåndtertAvSteg() {
        return STØTTEDE_VILKÅR;
    }

}
