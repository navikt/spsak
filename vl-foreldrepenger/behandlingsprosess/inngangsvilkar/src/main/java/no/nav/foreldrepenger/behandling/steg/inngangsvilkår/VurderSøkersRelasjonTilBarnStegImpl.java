package no.nav.foreldrepenger.behandling.steg.inngangsvilkår;

import static java.util.Arrays.asList;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.RegelOrkestrerer;


@BehandlingStegRef(kode = "VURDERSRB")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class VurderSøkersRelasjonTilBarnStegImpl extends InngangsvilkårStegImpl {

    static List<VilkårType> STØTTEDE_VILKÅR = asList(
        VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD,
        VilkårType.ADOPSJONSVILKARET_FORELDREPENGER,
        VilkårType.FØDSELSVILKÅRET_MOR,
        VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR,
        VilkårType.OMSORGSVILKÅRET,
        VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD,
        VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD);

    VurderSøkersRelasjonTilBarnStegImpl() {
        // for CDI proxy
    }

    @Inject
    public VurderSøkersRelasjonTilBarnStegImpl(BehandlingRepositoryProvider repositoryProvider, RegelOrkestrerer regelOrkestrerer) {
        super(repositoryProvider, regelOrkestrerer,BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
    }

    @Override
    public List<VilkårType> vilkårHåndtertAvSteg() {
        return STØTTEDE_VILKÅR;
    }

}
