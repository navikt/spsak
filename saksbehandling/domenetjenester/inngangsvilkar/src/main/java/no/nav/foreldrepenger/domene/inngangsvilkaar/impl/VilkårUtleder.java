package no.nav.foreldrepenger.domene.inngangsvilkaar.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface VilkårUtleder {
    UtledeteVilkår utledVilkår(Behandling behandling);
}
