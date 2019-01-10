package no.nav.foreldrepenger.domene.inngangsvilkaar;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

public interface RegelOrkestrerer {
    RegelResultat vurderInngangsvilkår(List<VilkårType> vilkårTyper, Behandling behandling, Behandlingsresultat behandlingsresultat);
}
