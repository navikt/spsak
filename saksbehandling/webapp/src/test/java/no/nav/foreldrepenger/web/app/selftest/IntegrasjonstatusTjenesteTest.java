package no.nav.foreldrepenger.web.app.selftest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.web.app.selftest.IntegrasjonstatusTjeneste;
import no.nav.foreldrepenger.web.app.selftest.SelftestResultat;
import no.nav.foreldrepenger.web.app.selftest.Selftests;
import no.nav.foreldrepenger.web.app.selftest.SystemNedeDto;

public class IntegrasjonstatusTjenesteTest {

    private Selftests selftests;
    private IntegrasjonstatusTjeneste integrasjonstatusTjeneste;

    @Before
    public void before() {
        selftests = mock(Selftests.class);
        integrasjonstatusTjeneste = new IntegrasjonstatusTjeneste(selftests);
    }

    @Test
    public void skal_kalle_oppdatering_og_returnere_info_om_system_som_er_nede() {
        // Arrange
        SelftestResultat selftestResultat = new SelftestResultat();
        HealthCheck.ResultBuilder systemSomErOppeBuilder = HealthCheck.Result.builder();
        systemSomErOppeBuilder.healthy()
            .withDetail("description", "Test av web service Arbeidsforhold")
            .withDetail("endpoint", "endpoint1");
        selftestResultat.leggTilResultatForKritiskTjeneste(systemSomErOppeBuilder.build());
        HealthCheck.ResultBuilder systemSomErNedeBuilder = HealthCheck.Result.builder();
        systemSomErNedeBuilder.unhealthy(new Exception("Feilmelding"))
            .withDetail("description", "Test av meldingskø for Økonomioppdrag Mottak")
            .withDetail("endpoint", "endpoint2");
        selftestResultat.leggTilResultatForKritiskTjeneste(systemSomErNedeBuilder.build());
        when(selftests.run()).thenReturn(selftestResultat);

        // Act
        List<SystemNedeDto> systemerSomErNede = integrasjonstatusTjeneste.finnSystemerSomErNede();

        // Assert
        assertThat(systemerSomErNede).hasSize(1);
        assertThat(systemerSomErNede.get(0).getSystemNavn()).isEqualTo("Økonomioppdrag Mottak");
        assertThat(systemerSomErNede.get(0).getFeilmelding()).isEqualTo("Feilmelding");
        assertThat(systemerSomErNede.get(0).getEndepunkt()).isEqualTo("endpoint2");
        assertThat(systemerSomErNede.get(0).getStackTrace()).contains("java.lang.Exception");
    }

    @Test
    public void skal_bruke_message_fra_result_hvis_throwable_ikke_er_oppgitt() {
        // Arrange
        SelftestResultat selftestResultat = new SelftestResultat();
        HealthCheck.ResultBuilder systemSomErNedeBuilder = HealthCheck.Result.builder();
        systemSomErNedeBuilder.unhealthy()
            .withDetail("description", "Test av meldingskø for Økonomioppdrag Mottak")
            .withDetail("endpoint", "endpoint2")
            .withMessage("Feilmelding fra message");
        selftestResultat.leggTilResultatForKritiskTjeneste(systemSomErNedeBuilder.build());
        when(selftests.run()).thenReturn(selftestResultat);

        // Act
        List<SystemNedeDto> systemerSomErNede = integrasjonstatusTjeneste.finnSystemerSomErNede();

        // Assert
        assertThat(systemerSomErNede).hasSize(1);
        assertThat(systemerSomErNede.get(0).getSystemNavn()).isEqualTo("Økonomioppdrag Mottak");
        assertThat(systemerSomErNede.get(0).getFeilmelding()).isEqualTo("Feilmelding fra message");
        assertThat(systemerSomErNede.get(0).getEndepunkt()).isEqualTo("endpoint2");
        assertThat(systemerSomErNede.get(0).getStackTrace()).isNull();
    }
}
