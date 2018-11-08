package no.nav.foreldrepenger.inngangsvilkaar.impl;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;

public interface VilkårUtleder {
    UtledeteVilkår utledVilkår(Behandling behandling, Optional<FamilieHendelseType> hendelseType);
}
