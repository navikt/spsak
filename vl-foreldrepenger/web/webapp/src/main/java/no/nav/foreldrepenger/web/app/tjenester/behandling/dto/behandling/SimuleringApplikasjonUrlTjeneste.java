package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class SimuleringApplikasjonUrlTjeneste {

    private String baseUrl;

    public SimuleringApplikasjonUrlTjeneste() {
        //for CDI proxy
    }

    @Inject
    public SimuleringApplikasjonUrlTjeneste(@KonfigVerdi("fpoppdrag.url") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUrlForSimuleringsresultat(){
        return baseUrl + "/fpoppdrag/api/simulering/resultat";
    }

}
