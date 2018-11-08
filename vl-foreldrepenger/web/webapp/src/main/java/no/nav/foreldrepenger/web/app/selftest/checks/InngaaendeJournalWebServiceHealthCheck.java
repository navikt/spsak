package no.nav.foreldrepenger.web.app.selftest.checks;

import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalSelftestConsumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InngaaendeJournalWebServiceHealthCheck extends WebServiceHealthCheck {

    private InngaaendeJournalSelftestConsumer selftestConsumer;

    InngaaendeJournalWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public InngaaendeJournalWebServiceHealthCheck(InngaaendeJournalSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Inngaaende Journal";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }
}
