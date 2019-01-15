package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.kodeverk.KodeverkSelftestConsumer;


@ApplicationScoped
public class KodeverkWebserviceHealthCheck extends WebServiceHealthCheck {

    private KodeverkSelftestConsumer selftestConsumer;

    KodeverkWebserviceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public KodeverkWebserviceHealthCheck(KodeverkSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Kodeverk";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }
}
