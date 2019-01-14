package no.nav.foreldrepenger.behandling.aksjonspunkt;

import java.util.Collection;

public interface AksjonspunktApplikasjonTjeneste {

    void bekreftAksjonspunkter(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Long behandlingId);

    void overstyrAksjonspunkter(Collection<OverstyringAksjonspunktDto> bekreftedeAksjonspunktDtoer, Long behandlingId);

}
