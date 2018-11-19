package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface ForeslåVedtakTjeneste {
    BehandleStegResultat foreslåVedtak(Behandling behandling);
}
