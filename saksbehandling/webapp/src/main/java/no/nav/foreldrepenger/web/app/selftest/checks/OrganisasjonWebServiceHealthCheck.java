package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonSelftestConsumer;

@ApplicationScoped
public class OrganisasjonWebServiceHealthCheck extends WebServiceHealthCheck{

    private OrganisasjonSelftestConsumer selftestConsumer;

    OrganisasjonWebServiceHealthCheck() {
        // for CDI
    }

    @Inject
    public OrganisasjonWebServiceHealthCheck(OrganisasjonSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Organisasjon";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }
}
