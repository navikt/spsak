package no.nav.foreldrepenger.behandling.brev;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class SendVarselEvent implements BehandlingEvent {

    private BehandlingskontrollKontekst kontekst;
    private String fritekst;
    private String brevType;

    public SendVarselEvent(BehandlingskontrollKontekst kontekst, String brevType) {
        this.kontekst = kontekst;
        this.brevType = brevType;
    }

    public SendVarselEvent(BehandlingskontrollKontekst kontekst, String fritekst, String brevType) {
        this.kontekst = kontekst;
        this.fritekst = fritekst;
        this.brevType = brevType;
    }

    @Override
    public Long getBehandlingId() {
        return kontekst.getBehandlingId();
    }

    @Override
    public Long getFagsakId() {
        return kontekst.getFagsakId();
    }

    @Override
    public AktørId getAktørId() {
        return kontekst.getAktørId();
    }

    public String getBrevType() {
        return brevType;
    }

    @Override
    public String toString() {
        return "SendVarselEvent{" +
            "kontekst=" + kontekst +
            ", brevType='" + brevType + '\'' +
            '}';
    }

    public String getFritekst() {
        return fritekst;
    }
}
