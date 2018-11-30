package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagSelftestConsumer;

public class MeldekortUtbetalingsgrunnlagWebServiceHealthCheckTest {

    @Test
    public void test_alt() {

        final String ENDPT = "http://test.erstatter";
        MeldekortUtbetalingsgrunnlagSelftestConsumer mockSelftestConsumer = mock(MeldekortUtbetalingsgrunnlagSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        MeldekortUtbetalingsgrunnlagWebServiceHealthCheck check = new MeldekortUtbetalingsgrunnlagWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new MeldekortUtbetalingsgrunnlagWebServiceHealthCheck(); // som trengs av CDI
    }
}
