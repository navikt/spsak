package no.nav.foreldrepenger.domene.kontrollerfakta;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

public interface StartpunktTjeneste {

    StartpunktType utledStartpunktMotOriginalBehandling(Behandling revurdering);
    StartpunktType utledStartpunktForDiffBehandlingsgrunnlag(Behandling behandling, EndringsresultatDiff differanse);
}
