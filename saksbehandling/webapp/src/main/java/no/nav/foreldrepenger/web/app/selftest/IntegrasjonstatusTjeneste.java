package no.nav.foreldrepenger.web.app.selftest;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.codahale.metrics.health.HealthCheck;

@ApplicationScoped
public class IntegrasjonstatusTjeneste {

    private static final String DESCRIPTION = "description";
    private static final String ENDPOINT = "endpoint";
    private static final List<String> PREFIKSER = asList("Test av web service ", "Test av meldingskø for ",
        "Test av web selftestConsumer ", "Test av ");

    private Selftests selftests;

    public IntegrasjonstatusTjeneste() {
        // CDI
    }

    @Inject
    public IntegrasjonstatusTjeneste(Selftests selftests) {
        this.selftests = selftests;
    }

    public List<SystemNedeDto> finnSystemerSomErNede() {
        List<SystemNedeDto> systemerSomErNede = new ArrayList<>();

        SelftestResultat selftestResultat = selftests.run();
        List<HealthCheck.Result> alleResultater = selftestResultat.getAlleResultater();
        for (HealthCheck.Result resultat : alleResultater) {
            if (!resultat.isHealthy()) {
                systemerSomErNede.add(lagDto(resultat));
            }
        }

        return systemerSomErNede;
    }

    private SystemNedeDto lagDto(HealthCheck.Result resultat) {
        String systemNavn = getSystemNavn(resultat);
        String endepunkt = getEndepunkt(resultat);
        Throwable throwable = resultat.getError();
        if (throwable != null) {
            return new SystemNedeDto(systemNavn, endepunkt, null, throwable.getMessage(), getStackTrace(throwable));
        } else {
            return new SystemNedeDto(systemNavn, endepunkt, null, resultat.getMessage(), null);
        }
    }

    private String getSystemNavn(HealthCheck.Result resultat) {
        String description = (String) resultat.getDetails().get(DESCRIPTION);
        String systemNavn = description;
        for (String prefiks : PREFIKSER) {
            if (description.startsWith(prefiks)) {
                systemNavn = description.replaceAll(prefiks, "");
                break;
            }
        }
        return systemNavn;
    }

    private String getEndepunkt(HealthCheck.Result resultat) {
        return (String) resultat.getDetails().get(ENDPOINT);
    }

    private String getStackTrace(Throwable throwable) {
        return ExceptionUtils.getStackTrace(throwable);
    }
}
