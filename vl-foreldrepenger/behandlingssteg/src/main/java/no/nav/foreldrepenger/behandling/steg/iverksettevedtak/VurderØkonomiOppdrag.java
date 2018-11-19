package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;

public abstract class VurderØkonomiOppdrag {

    protected VurderØkonomiOppdrag() {
    }

    protected abstract boolean skalSendeOppdrag(Behandling behandling, BehandlingVedtak behandlingVedtak);
}
