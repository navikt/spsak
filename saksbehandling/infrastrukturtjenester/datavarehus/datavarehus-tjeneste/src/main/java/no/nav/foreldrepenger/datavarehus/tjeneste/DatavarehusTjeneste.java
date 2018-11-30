package no.nav.foreldrepenger.datavarehus.tjeneste;

import java.util.Collection;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;

public interface DatavarehusTjeneste {

    void lagreNedFagsak(Long fagsakId);

    void lagreNedAksjonspunkter(Collection<Aksjonspunkt> aksjonspunkter, Long behandlingId, BehandlingStegType behandlingStegType);

    void lagreNedBehandlingStegTilstand(BehandlingStegTilstand tilTilstand);

    void lagreNedBehandling(Long behandlingId);

    void lagreNedBehandling(Behandling behandling);

    void lagreNedVedtak(BehandlingVedtak vedtak, Long behandlingId);

    void lagreNedBehandlingOgTilstander(Long behandlingId);

}
