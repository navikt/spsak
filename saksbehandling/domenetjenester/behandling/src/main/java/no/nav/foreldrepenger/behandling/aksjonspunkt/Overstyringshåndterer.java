package no.nav.foreldrepenger.behandling.aksjonspunkt;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface Overstyringshåndterer<T extends OverstyringAksjonspunktDto> {

    OppdateringResultat håndterOverstyring(T dto, Behandling behandling, BehandlingskontrollKontekst kontekst);

    /** Opprett Aksjonspunkt for Overstyring og håndter lagre historikk. */
    void håndterAksjonspunktForOverstyring(T dto, Behandling behandling);
}
