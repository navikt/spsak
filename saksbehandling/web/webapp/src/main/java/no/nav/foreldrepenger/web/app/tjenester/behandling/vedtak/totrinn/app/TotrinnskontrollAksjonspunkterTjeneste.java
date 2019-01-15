package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.app;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.dto.TotrinnskontrollSkjermlenkeContextDto;

public interface TotrinnskontrollAksjonspunkterTjeneste {

    List<TotrinnskontrollSkjermlenkeContextDto>
    hentTotrinnsSkjermlenkeContext(Behandling behandling);

    List<TotrinnskontrollSkjermlenkeContextDto>
    hentTotrinnsvurderingSkjermlenkeContext(Behandling behandling);
}
