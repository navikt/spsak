package no.nav.vedtak.sikkerhet.abac;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class PepImpl implements Pep {

    private PdpKlient pdpKlient;
    private PdpRequestBuilder pdpRequestBuilder;

    public PepImpl() {
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient, PdpRequestBuilder pdpRequestBuilder) {
        this.pdpKlient = pdpKlient;
        this.pdpRequestBuilder = pdpRequestBuilder;
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(AbacAttributtSamling attributter) {
        validerInput(attributter);
        PdpRequest pdpRequest = pdpRequestBuilder.lagPdpRequest(attributter);
        return pdpKlient.forespørTilgang(pdpRequest);
    }

    private void validerInput(AbacAttributtSamling attributter) {
        if (attributter.getBehandlingsIder().size() > 1) {
            throw PepFeil.FACTORY.ugyldigInputForMangeBehandlingIder(attributter.getBehandlingsIder()).toException();
        }
    }


}
