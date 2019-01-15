package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.medl.MedlemSelftestConsumer;

public class MedlemWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.erstatter";
        MedlemSelftestConsumer mockSelftestConsumer = mock(MedlemSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        MedlemWebServiceHealthCheck check = new MedlemWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new MedlemWebServiceHealthCheck(); // som trengs av CDI
    }
}
