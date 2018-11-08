package no.nav.foreldrepenger.domene.kontrollerfakta;

import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;

public interface BehandlingÅrsakTjeneste {

    Set<BehandlingÅrsakType> utledBehandlingÅrsakerMotOriginalBehandling(Behandling revurdering);
    Set<BehandlingÅrsakType> utledBehandlingÅrsakerBasertPåDiff(Behandling behanlding, EndringsresultatDiff endringsresultat);
}
