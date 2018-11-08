package no.nav.foreldrepenger.web.app.selftest.checks;

import no.nav.vedtak.felles.integrasjon.sakogbehandling.SakOgBehandlingClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SakOgBehandlingQueueHealthCheckTest {

    @Test
    public void test_alt() {
        SakOgBehandlingClient mockClient = mock(SakOgBehandlingClient.class);
        SakOgBehandlingQueueHealthCheck check = new SakOgBehandlingQueueHealthCheck(mockClient);

        assertThat(check.getDescriptionSuffix()).isNotNull();

        new SakOgBehandlingQueueHealthCheck(); // som CDI trenger
    }
}
