package no.nav.foreldrepenger.web.app.selftest.checks;

import no.nav.vedtak.felles.integrasjon.medl.MedlemSelftestConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MedlemWebServiceHealthCheck extends WebServiceHealthCheck {

    private MedlemSelftestConsumer selftestConsumer;

    MedlemWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public MedlemWebServiceHealthCheck(MedlemSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Medlem (MEDL2)";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }
}
