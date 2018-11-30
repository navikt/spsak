package no.nav.foreldrepenger.behandling.steg.vedtak;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface FatteVedtakTjeneste {
    BehandleStegResultat fattVedtak(BehandlingskontrollKontekst kontekst, Behandling behandling);
}
