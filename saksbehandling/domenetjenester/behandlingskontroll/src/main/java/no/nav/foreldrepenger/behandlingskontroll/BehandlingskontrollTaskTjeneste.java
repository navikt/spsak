package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class BehandlingskontrollTaskTjeneste {

    private ProsessTaskRepository prosessTaskRepository;

    BehandlingskontrollTaskTjeneste() {
        // for CDI proxy
    }

    @Inject
    public BehandlingskontrollTaskTjeneste(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    /**
         * Opprett og lagre en StartBehandlingTask (kaller prosesserBehandling) i ny ProsessTaskGruppe.
         * Forutsetter initialtilstand. Returnerer gruppe.
         */
    public String opprettStartBehandlingTask(Long fagsakId, Long behandlingId, AktørId aktør) {
        ProsessTaskData taskData = new ProsessTaskData(StartBehandlingTask.TASKTYPE);
        taskData.setBehandling(fagsakId, behandlingId, aktør.getId());
        return lagreMedCallId(taskData);
    }

    /**
         * Kjør prosess asynkront (i egen prosess task) videre.
         */
    public String opprettFortsettBehandlingTaskNesteSekvens(ProsessTaskData prosessTaskData) {
        ProsessTaskData taskData = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        taskData.setBehandling(prosessTaskData.getFagsakId(), prosessTaskData.getBehandlingId(), prosessTaskData.getAktørId());
        taskData.setGruppe(prosessTaskData.getGruppe());
        taskData.setSekvens(String.valueOf(Integer.parseInt(prosessTaskData.getSekvens()) + 1));  // increment 1
        return lagreMedCallId(taskData);
    }

    public String opprettFortsettBehandlingTask(Long fagsakId, Long behandlingId, AktørId aktør, Optional<AksjonspunktDefinisjon> autopunktUtført) {
        ProsessTaskData taskData = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        taskData.setBehandling(fagsakId, behandlingId, aktør.getId());
        autopunktUtført.ifPresent(apu -> {
            taskData.setProperty(FortsettBehandlingTaskProperties.UTFORT_AUTOPUNKT, apu.getKode());
        });
        return lagreMedCallId(taskData);
    }

    // Vurder opprettFortsettBehandlingTaskAutopunkterUtført - set property FortsettBehandlingTaskProperties.MANUELL_FORTSETTELSE

    /**
         * Kjør prosess asynkront (i egen prosess task) videre.
         */
    public String opprettBehandlingskontrollTask(String taskType, Long fagsakId, Long behandlingId, AktørId aktør) {
        ProsessTaskData taskData = new ProsessTaskData(taskType);
        taskData.setBehandling(fagsakId, behandlingId, aktør.getId());

        return lagreMedCallId(taskData);
    }

    private String lagreMedCallId(ProsessTaskData prosessTaskData) {
        prosessTaskData.setCallIdFraEksisterende();
        return prosessTaskRepository.lagre(prosessTaskData);
    }
}
