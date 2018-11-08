package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface IverksetteVedtakHistorikkTjeneste {

    void opprettHistorikkinnslagNårIverksettelsePåVent(Behandling behandling, boolean venterTidligereBehandling, boolean kanIverksettes);
}
