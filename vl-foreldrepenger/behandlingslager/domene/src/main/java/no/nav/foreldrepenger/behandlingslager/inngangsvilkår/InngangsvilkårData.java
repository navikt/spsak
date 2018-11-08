package no.nav.foreldrepenger.behandlingslager.inngangsvilkår;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;

public class InngangsvilkårData {
    private final VilkårResultatType vilkårResultatType;
    private final List<VilkårData> vilkårDefinisjoner;

    public InngangsvilkårData(VilkårResultatType vilkårResultatType, List<VilkårData> vilkårSpeker) {
        this.vilkårResultatType = vilkårResultatType;
        this.vilkårDefinisjoner = vilkårSpeker;
    }

    public VilkårResultatType getVilkårResultatType() {
        return vilkårResultatType;
    }

    public List<VilkårData> getVilkårData() {
        return vilkårDefinisjoner;
    }
}
