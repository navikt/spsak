package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalSelftestConsumer;

public class InngaaendeJournalWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.erstatter";
        InngaaendeJournalSelftestConsumer mockSelftestConsumer = mock(InngaaendeJournalSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        InngaaendeJournalWebServiceHealthCheck check = new InngaaendeJournalWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new InngaaendeJournalWebServiceHealthCheck(); // som trengs av CDI
    }
}
