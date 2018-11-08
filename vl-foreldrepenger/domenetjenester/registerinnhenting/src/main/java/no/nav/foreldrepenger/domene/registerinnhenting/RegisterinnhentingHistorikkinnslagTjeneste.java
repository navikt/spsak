package no.nav.foreldrepenger.domene.registerinnhenting;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;

public interface RegisterinnhentingHistorikkinnslagTjeneste {

    void opprettHistorikkinnslagForNyeRegisteropplysninger(Behandling behandling);

    void opprettHistorikkinnslagForTilbakespoling(Behandling behandling, BehandlingStegType førSteg, BehandlingStegType etterSteg);

    void opprettHistorikkinnslagForBehandlingMedNyeOpplysninger(Behandling behandling, BehandlingÅrsakType behandlingÅrsakType);
}
