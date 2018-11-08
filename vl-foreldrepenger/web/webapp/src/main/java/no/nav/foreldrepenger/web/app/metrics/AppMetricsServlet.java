package no.nav.foreldrepenger.web.app.metrics;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

import no.nav.foreldrepenger.behandlingslager.behandling.Tema;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

/**
 * Implementasjon som automatisk setter UTF-8 encoding for JSON resultat.
 */
@ApplicationScoped
public class AppMetricsServlet extends MetricsServlet {

    private static final String KEY_PROSESSTASK = "prosesstask";
    private static final String PROSESS_TASK_TYPE_PREFIX_FORDELING = "fordeling";
    private static final List<String> PROSESS_TASK_TYPE_PREFIXES = Arrays.asList(
        "behandlingskontroll", PROSESS_TASK_TYPE_PREFIX_FORDELING, "innhentsaksopplysninger", "integrasjon", "iverksetteVedtak", "oppgavebehandling");
    private static final String KEY_PREFIX = "fpsak";
    private static final String KEY_APNE_BEHANDLINGER = "apne.behandlinger";
    private static final String KEY_APNE_OPPGAVER = "apne.oppgaver";
    private static final String KEY_VENTENDE_BEHANDLINGER = "ventende.behandlinger";
    private MetricRepository metricRepository; // NOSONAR
    private MetricRegistry registry;  // NOSONAR
    private KodeverkRepository kodeverkRepository; //NOSONAR
    private ProsessTaskGaugesCache prosessTaskGaugesCache; //NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        super.doGet(req, resp);  // NOSONAR
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        registrerLettereMetricsForÅpneOgVentendeBehandlinger();
        registrerMetricsForKøedeOgFeiledeProsessTasks();
        registrerLettereMetricsForKøedeOgFeiledeProsessTasks();
        super.init(config);
    }

    private void registrerLettereMetricsForÅpneOgVentendeBehandlinger() {
        String tema = kodeverkRepository.finn(Tema.class, Tema.FORELDRE_OG_SVANGERSKAPSPENGER).getOffisiellKode();

        registry.register(KEY_PREFIX + "." + tema + "." + KEY_APNE_BEHANDLINGER,
            (Gauge<BigDecimal>) () -> metricRepository.tellLettereAntallBehandlingerSomIkkeHarBlittAvsluttet());
        registry.register(KEY_PREFIX + "." + tema + "." + KEY_VENTENDE_BEHANDLINGER,
            (Gauge<BigDecimal>) () -> metricRepository.tellLettereAntallVentendeBehandlinger());
        registry.register(KEY_PREFIX + "." + tema + "." + KEY_APNE_OPPGAVER,
            (Gauge<BigDecimal>) () -> metricRepository.tellLettereAntallVentendeOppgaver());
    }

    private void registrerLettereMetricsForKøedeOgFeiledeProsessTasks() {
        registry.register(KEY_PREFIX + "." + KEY_PROSESSTASK + ".allekoet" ,
            (Gauge<BigDecimal>) () -> prosessTaskGaugesCache.antallProsessTaskerKøet()); //NOSONAR
        registry.register(KEY_PREFIX + "." + KEY_PROSESSTASK + ".allefeilet",
            (Gauge<BigDecimal>) () -> prosessTaskGaugesCache.antallProsessTaskerFeilet()); //NOSONAR
    }


    private void registrerMetricsForKøedeOgFeiledeProsessTasks() {
        List<String> prosessTaskTyper = metricRepository.hentProsessTaskTyperMedPrefixer(PROSESS_TASK_TYPE_PREFIXES);
        for (String ptType : prosessTaskTyper) {
            registry.register(KEY_PREFIX + "." + KEY_PROSESSTASK + ".koet." + ptType, (Gauge<BigDecimal>) () -> prosessTaskGaugesCache.antallProsessTaskerKøet(ptType));
            registry.register(KEY_PREFIX + "." + KEY_PROSESSTASK + ".feilet." + ptType, (Gauge<BigDecimal>) () -> prosessTaskGaugesCache.antallProsessTaskerFeilet(ptType));
        }

        registry.register(KEY_PREFIX + "." + KEY_PROSESSTASK + ".koet." + PROSESS_TASK_TYPE_PREFIX_FORDELING,
            (Gauge<BigDecimal>) () -> prosessTaskGaugesCache.antallProsessTaskerMedTypePrefixKøet(PROSESS_TASK_TYPE_PREFIX_FORDELING));
    }

    @Inject
    public void setMetricRepository(MetricRepository metricRepository) {
        this.metricRepository = metricRepository; //NOSONAR
    }

    @Inject
    public void setKodeverkRepository(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository; //NOSONAR
    }

    @Inject
    public void setRegistry(MetricRegistry registry) {
        this.registry = registry; //NOSONAR
    }

    @Inject
    public void setProsessTaskGaugesCache(ProsessTaskGaugesCache prosessTaskGaugesCache) {
        this.prosessTaskGaugesCache = prosessTaskGaugesCache; //NOSONAR
    }
}
