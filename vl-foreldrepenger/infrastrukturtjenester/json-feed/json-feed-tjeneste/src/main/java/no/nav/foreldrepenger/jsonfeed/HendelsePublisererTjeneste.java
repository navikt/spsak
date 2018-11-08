package no.nav.foreldrepenger.jsonfeed;

import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEvent;

public interface HendelsePublisererTjeneste {
    void lagreVedtak(BehandlingVedtak vedtak);

    void lagreFagsakAvsluttet(FagsakStatusEvent event);
}
