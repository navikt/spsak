package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import java.util.Collection;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;

public interface AksjonspunktApplikasjonTjeneste {

    void bekreftAksjonspunkter(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Long behandlingId);

    void overstyrAksjonspunkter(Collection<OverstyringAksjonspunktDto> bekreftedeAksjonspunktDtoer, Long behandlingId);

}
