package no.nav.foreldrepenger.web.app.startupinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.jboss.resteasy.annotations.Query;
import org.jboss.weld.util.reflection.Formats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.web.app.selftest.SelftestResultat;
import no.nav.foreldrepenger.web.app.selftest.Selftests;
import no.nav.foreldrepenger.web.app.selftest.checks.ExtHealthCheck;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;

/** Dependent scope siden vi lukker denne når vi er ferdig. */
@Dependent
class AppStartupInfoLogger {

    private static final Logger logger = LoggerFactory.getLogger(AppStartupInfoLogger.class);

    private Selftests selftests;

    private static final String OPPSTARTSINFO = "OPPSTARTSINFO";
    private static final String HILITE_SLUTT = "********";
    private static final String HILITE_START = HILITE_SLUTT;
    private static final String KONFIGURASJON = "Konfigurasjon";
    private static final String SELFTEST = "Selftest";
    private static final String APPLIKASJONENS_STATUS = "Applikasjonens status";
    private static final String SYSPROP = "System property";
    private static final String ENVVAR = "Env. var";
    private static final String START = "start:";
    private static final String SLUTT = "slutt.";

    private static final String SKIP_LOG_SYS_PROPS = "skipLogSysProps";
    private static final String SKIP_LOG_ENV_VARS = "skipLogEnvVars";
    private static final String TRUE = "true";
    
    /** Samler opp all logging og outputter til slutt. */
    private List<Runnable> logStatements = new ArrayList<>();

    AppStartupInfoLogger() {
        // for CDI proxy
    }

    @Inject
    AppStartupInfoLogger(Selftests selftests) {
        this.selftests = selftests;
    }

    void logAppStartupInfo() {
        log(HILITE_START + " " + OPPSTARTSINFO + " " + START + " " + HILITE_SLUTT);
        logVersjoner();
        logKonfigurasjon();
        logSelftest();
        log(HILITE_START + " " + OPPSTARTSINFO + " " + SLUTT + " " + HILITE_SLUTT);
        
        writeLog();
    }

    private void writeLog() {
        logStatements.forEach(r -> r.run());
    }

    private void logVersjoner() {
        // Noen biblioteker er bundlet med jboss og kan skape konflikter, eller jboss overstyrer vår overstyring via modul classpath
        // her logges derfor hva som er effektivt tilgjengelig av ulike biblioteker som kan være påvirket ved oppstart
        log("Bibliotek: Hibernate: {}", org.hibernate.Version.getVersionString());
        log("Bibliotek: Weld: {}", Formats.version(null));
        log("Bibliotek: CDI: {}", CDI.class.getPackage().getImplementationVendor() + ":" + CDI.class.getPackage().getSpecificationVersion());
        log("Bibliotek: Resteasy: {}", Query.class.getPackage().getImplementationVersion()); // tilfeldig valgt Resteasy klasse
    }

    private void logKonfigurasjon() {
        log(KONFIGURASJON + " " + START);

        SystemPropertiesHelper sysPropsHelper = SystemPropertiesHelper.getInstance();
        boolean skipSysProps = TRUE.equalsIgnoreCase(System.getProperty(SKIP_LOG_SYS_PROPS));
        boolean skipEnvVars = TRUE.equalsIgnoreCase(System.getProperty(SKIP_LOG_ENV_VARS));

        if (!skipSysProps) {
            SortedMap<String, String> sysPropsMap = sysPropsHelper.filteredSortedProperties();
            String sysPropFormat = SYSPROP + ": {}={}";
            for (Entry<String, String> entry : sysPropsMap.entrySet()) {
                log(sysPropFormat, LoggerUtils.removeLineBreaks(entry.getKey()), LoggerUtils.removeLineBreaks(entry.getValue()));
            }
        }

        if (!skipEnvVars) {
            SortedMap<String, String> envVarsMap = sysPropsHelper.filteredSortedEnvVars();
            for (Entry<String, String> entry : envVarsMap.entrySet()) {
                String envVarFormat = ENVVAR + ": {}={}";
                log(envVarFormat, LoggerUtils.removeLineBreaks(entry.getKey()), LoggerUtils.removeLineBreaks(entry.getValue()));
            }
        }

        log(KONFIGURASJON + " " + SLUTT);
    }

    private void logSelftest() {
        log(SELFTEST + " " + START);

        // callId er påkrevd på utgående kall og må settes før selftest kjøres
        MDCOperations.putCallId();
        SelftestResultat samletResultat = selftests.run();
        MDCOperations.removeCallId();

        for (HealthCheck.Result result : samletResultat.getAlleResultater()) {
            log(result);
        }

        log(APPLIKASJONENS_STATUS + ": {}", samletResultat.getAggregateResult());

        log(SELFTEST + " " + SLUTT);
    }

    private void log(String msg, Object... args) {
        if (args == null || args.length == 0) {
            // skiller ut ellers logger logback ekstra paranteser og fnutter for tomme args
            logStatements.add(() -> logger.info(msg));
        } else {
            logStatements.add(() ->logger.info(msg, args));
        }
    }

    private void log(HealthCheck.Result result) {
        Feil feil;
        if (result.getDetails() != null) {
            feil = OppstartFeil.FACTORY.selftestStatus(
                getStatus(result.isHealthy()),
                (String) result.getDetails().get(ExtHealthCheck.DETAIL_DESCRIPTION),
                (String) result.getDetails().get(ExtHealthCheck.DETAIL_ENDPOINT),
                (String) result.getDetails().get(ExtHealthCheck.DETAIL_RESPONSE_TIME),
                result.getMessage());
        } else {
            feil = OppstartFeil.FACTORY.selftestStatus(
                getStatus(result.isHealthy()),
                null,
                null,
                null,
                result.getMessage());
        }
        
        logStatements.add(() -> feil.log(logger));
    }

    private String getStatus(boolean isHealthy) {
        return isHealthy ? "OK" : "ERROR";
    }
}
