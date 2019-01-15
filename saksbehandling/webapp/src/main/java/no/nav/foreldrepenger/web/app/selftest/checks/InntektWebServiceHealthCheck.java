package no.nav.foreldrepenger.web.app.selftest.checks;

import no.nav.vedtak.felles.integrasjon.inntekt.InntektSelftestConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InntektWebServiceHealthCheck extends WebServiceHealthCheck {

    private InntektSelftestConsumer selftestConsumer;

    InntektWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public InntektWebServiceHealthCheck(InntektSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Inntekt (Inntektskomponenten)";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }
}
