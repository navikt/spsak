package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;

public interface BehandlingÅrsakUtleder {
    Set<BehandlingÅrsakType> utledBehandlingÅrsaker(Behandling behandling, Long grunnlagId1, Long grunnlagId2);
}
