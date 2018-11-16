package no.nav.foreldrepenger.inngangsvilkaar.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface VilkårUtleder {
    UtledeteVilkår utledVilkår(Behandling behandling);
}
