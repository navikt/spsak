package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.sakogbehandling.SakOgBehandlingClient;

@ApplicationScoped
public class SakOgBehandlingQueueHealthCheck extends QueueHealthCheck {

    SakOgBehandlingQueueHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public SakOgBehandlingQueueHealthCheck(SakOgBehandlingClient client) {
        super(client);
    }

    @Override
    protected String getDescriptionSuffix() {
        return "Sak og behandling hendelse";
    }
}
