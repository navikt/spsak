package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.journal.v3.JournalSelftestConsumer;

@ApplicationScoped
public class JournalWebServiceHealthCheck extends WebServiceHealthCheck {

    private JournalSelftestConsumer selftestConsumer;

    JournalWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public JournalWebServiceHealthCheck(JournalSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Journal";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }
}
