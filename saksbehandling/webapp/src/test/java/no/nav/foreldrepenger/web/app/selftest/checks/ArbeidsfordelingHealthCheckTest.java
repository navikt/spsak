package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient.ArbeidsfordelingSelftestConsumer;

public class ArbeidsfordelingHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.erstatter";
        ArbeidsfordelingSelftestConsumer mockSelftestConsumer = mock(ArbeidsfordelingSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        ArbeidsfordelingHealthCheck check = new ArbeidsfordelingHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new ArbeidsfordelingHealthCheck(); // som trengs av CDI
    }
}
