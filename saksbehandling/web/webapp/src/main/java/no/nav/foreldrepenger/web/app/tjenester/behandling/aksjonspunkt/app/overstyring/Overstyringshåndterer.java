package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;

public interface Overstyringshåndterer<T extends OverstyringAksjonspunktDto> {

    OppdateringResultat håndterOverstyring(T dto, Behandling behandling, BehandlingskontrollKontekst kontekst);

    /** Opprett Aksjonspunkt for Overstyring og håndter lagre historikk. */
    void håndterAksjonspunktForOverstyring(T dto, Behandling behandling);
}
