package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdSelftestConsumer;

public class ArbeidsforholdWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.erstatter";

        ArbeidsforholdSelftestConsumer mockSelftestConsumer = mock(ArbeidsforholdSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        ArbeidsforholdWebServiceHealthCheck check = new ArbeidsforholdWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new ArbeidsforholdWebServiceHealthCheck(); // for CDI
    }
}
