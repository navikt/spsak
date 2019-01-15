package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.behandlesak.klient.BehandleSakSelftestConsumer;

@ApplicationScoped
public class BehandleSakWebServiceHealthCheck extends WebServiceHealthCheck {

    private BehandleSakSelftestConsumer behandleSakSelftestConsumer;

    BehandleSakWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public BehandleSakWebServiceHealthCheck(BehandleSakSelftestConsumer behandleSakSelftestConsumer) {
        this.behandleSakSelftestConsumer = behandleSakSelftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        behandleSakSelftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service BehandleSak (GSAK)";
    }

    @Override
    protected String getEndpoint() {
        return behandleSakSelftestConsumer.getEndpointUrl();
    }
}
