package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonSelftestConsumer;

public class OrganisasjonWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.erstatter";

        OrganisasjonSelftestConsumer mockSelftestConsumer = mock(OrganisasjonSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        OrganisasjonWebServiceHealthCheck check = new OrganisasjonWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new OrganisasjonWebServiceHealthCheck(); // for CDI
    }
}
