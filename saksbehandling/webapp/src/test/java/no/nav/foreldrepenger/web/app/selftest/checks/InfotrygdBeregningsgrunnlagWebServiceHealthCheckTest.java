package no.nav.foreldrepenger.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag.InfotrygdBeregningsgrunnlagSelftestConsumer;

public class InfotrygdBeregningsgrunnlagWebServiceHealthCheckTest {

    @Test
    public void test_alt() {

        final String ENDPT = "http://test.erstatter";
        InfotrygdBeregningsgrunnlagSelftestConsumer mockSelftestConsumer = mock(InfotrygdBeregningsgrunnlagSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        InfotrygdBeregningsgrunnlagWebServiceHealthCheck check = new InfotrygdBeregningsgrunnlagWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new InfotrygdBeregningsgrunnlagWebServiceHealthCheck(); // som trengs av CDI
    }
}
