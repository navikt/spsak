package no.nav.foreldrepenger.behandling.steg.vedtak;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;

public interface BehandlingVedtakTjeneste {

    /**
     * Opprett {@link BehandlingVedtak} for en gitt behandling
     * @param kontekst Container som holder kontekst under prosessering av {@link BehandlingSteg}.
     * @param behandling en behandling.
     */
    void opprettBehandlingVedtak(BehandlingskontrollKontekst kontekst, Behandling behandling);
}
