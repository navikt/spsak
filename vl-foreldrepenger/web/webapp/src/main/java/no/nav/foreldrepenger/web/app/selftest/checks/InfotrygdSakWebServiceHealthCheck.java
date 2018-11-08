package no.nav.foreldrepenger.web.app.selftest.checks;

import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakSelftestConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InfotrygdSakWebServiceHealthCheck extends WebServiceHealthCheck {

    InfotrygdSakSelftestConsumer infotrygdSakSelftestConsumer;

    InfotrygdSakWebServiceHealthCheck(){
        //For CDI proxy
    }

    @Inject
    public InfotrygdSakWebServiceHealthCheck(InfotrygdSakSelftestConsumer infotrygdSakSelftestConsumer){
        this.infotrygdSakSelftestConsumer = infotrygdSakSelftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        infotrygdSakSelftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service InfotrygdSak";
    }

    @Override
    protected String getEndpoint() {
        return infotrygdSakSelftestConsumer.getEndpointUrl();
    }

}
