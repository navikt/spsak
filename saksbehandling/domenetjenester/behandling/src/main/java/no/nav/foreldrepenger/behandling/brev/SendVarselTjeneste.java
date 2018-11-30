package no.nav.foreldrepenger.behandling.brev;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;

@ApplicationScoped
public class SendVarselTjeneste {

    private SendVarselEventPubliserer sendVarselEventPubliserer;

    SendVarselTjeneste() {
    }

    @Inject
    public SendVarselTjeneste(SendVarselEventPubliserer sendVarselEventPubliserer) {
        this.sendVarselEventPubliserer = sendVarselEventPubliserer;
    }

    public void sendVarsel(Long behandlingId, String varselType) {
        BehandlingskontrollKontekst kontekst = getBehandlingskontrollTjeneste().initBehandlingskontroll(behandlingId);
        sendVarselEventPubliserer.fireEvent(new SendVarselEvent(kontekst, varselType));
    }

    public void sendVarsel(Long behandlingId, String varselType, String fritekst) {
        BehandlingskontrollKontekst kontekst = getBehandlingskontrollTjeneste().initBehandlingskontroll(behandlingId);
        sendVarselEventPubliserer.fireEvent(new SendVarselEvent(kontekst, varselType, fritekst));
    }

    private BehandlingskontrollTjeneste getBehandlingskontrollTjeneste() {
        CDI<Object> cdi = CDI.current();
        return cdi.select(BehandlingskontrollTjeneste.class).get();
    }
}
