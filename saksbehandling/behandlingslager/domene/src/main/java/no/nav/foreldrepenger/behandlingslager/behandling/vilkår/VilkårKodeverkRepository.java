package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

public interface VilkårKodeverkRepository extends BehandlingslagerRepository {

    VilkårResultatType finnVilkårResultatType(String kode);

    VilkårType finnVilkårType(String kode);

    VilkårUtfallType finnVilkårUtfallType(String kode);

    AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode);

    VilkårUtfallMerknad finnVilkårUtfallMerknad(String kode);

    List<Avslagsårsak> finnAvslagÅrsakListe(String vilkårType);

    Map<VilkårType, List<Avslagsårsak>> finnAvslagårsakerGruppertPåVilkårType();

    Optional<Avslagsårsak> finnEnesteAvslagÅrsak(String vilkårType);

    List<VilkårType> finnVilkårTypeListe(String avslagsårsakKode);

    Avslagsårsak finnAvslagÅrsak(String avslagKode);

    KodeverkRepository getKodeverkRepository();

}
