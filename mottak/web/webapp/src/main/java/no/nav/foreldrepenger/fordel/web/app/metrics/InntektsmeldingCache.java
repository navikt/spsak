package no.nav.foreldrepenger.fordel.web.app.metrics;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.task.KlargjorForVLTask;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class InntektsmeldingCache {

    private MetricRepository metricRepository;

    private static final long MAX_DATA_ALDER_MS = 10000;

    private Map<String, Long> total;
    private Map<String, Long> dagens;
    private Map<Fagsystem, String> taskNames;

    private long antallLestTidspunktMs = 0;

    InntektsmeldingCache() {
        // for CDI proxy
    }

    @Inject
    public InntektsmeldingCache(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
        this.total = new HashMap<>();
        this.dagens = new HashMap<>();
        this.taskNames = new HashMap<>();
        taskNames.put(Fagsystem.GOSYS, OpprettGSakOppgaveTask.TASKNAME);
        taskNames.put(Fagsystem.FPSAK, KlargjorForVLTask.TASKNAME);
    }


    public Long hentInntektsMeldingerSendtTilSystem(Fagsystem fagsystem, LocalDate dag) {
        refreshDataIfNeeded();
        String task = taskNames.get(fagsystem);
        return dag == null ? total.getOrDefault(task, 0l) : dagens.getOrDefault(task, 0l);
    }

    private void refreshDataIfNeeded() {
        long naaMs = System.currentTimeMillis();
        long alderMs = naaMs - antallLestTidspunktMs;
        if (alderMs >= MAX_DATA_ALDER_MS) {
            for (String task : taskNames.values()) {
                dagens.put(task, metricRepository.tellAntallDokumentTypeIdForTaskType(task, DokumentTypeId.INNTEKTSMELDING.getKode(), FPDateUtil.iDag()));
                total.put(task, metricRepository.tellAntallDokumentTypeIdForTaskType(task, DokumentTypeId.INNTEKTSMELDING.getKode(), null));
            }
            antallLestTidspunktMs = System.currentTimeMillis();
        }
    }
}
