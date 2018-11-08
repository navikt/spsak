package no.nav.foreldrepenger.behandlingslager.inngangsvilkår;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

public interface RegelOrkestrerer {
    RegelResultat vurderInngangsvilkår(List<VilkårType> vilkårTyper, Behandling behandling);
}
