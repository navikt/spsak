package no.nav.foreldrepenger.domene.inngangsvilkaar;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

public class RegelResultat {
    private final VilkårResultat vilkårResultat;
    private final List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner;

    private final Map<VilkårType, Object> ekstraResultater;

    public RegelResultat(VilkårResultat vilkårResultat, List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner,
            Map<VilkårType, Object> ekstraResultater) {
        this.vilkårResultat = vilkårResultat;
        this.aksjonspunktDefinisjoner = aksjonspunktDefinisjoner;
        this.ekstraResultater = ekstraResultater;
    }

    public VilkårResultat getVilkårResultat() {
        return vilkårResultat;
    }

    public Map<VilkårType, Object> getEkstraResultater() {
        return Collections.unmodifiableMap(ekstraResultater);
    }

    public List<AksjonspunktDefinisjon> getAksjonspunktDefinisjoner() {
        return aksjonspunktDefinisjoner;
    }

    public <V> Optional<V> getEkstraResultat(VilkårType vilkårType) {
        @SuppressWarnings("unchecked")
        V val = (V) ekstraResultater.get(vilkårType);
        return Optional.ofNullable(val);
    }
}
