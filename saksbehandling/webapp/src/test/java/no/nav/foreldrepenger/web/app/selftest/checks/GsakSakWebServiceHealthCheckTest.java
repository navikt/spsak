package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakSelftestConsumer;

public class GsakSakWebServiceHealthCheckTest {

    @Test
    public void test_alt() {

        final String ENDPT = "http://test.erstatter";
        InfotrygdSakSelftestConsumer mockSelftestConsumer = mock(InfotrygdSakSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        InfotrygdSakWebServiceHealthCheck check = new InfotrygdSakWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new InfotrygdSakWebServiceHealthCheck(); // som trengs av CDI
    }
}
