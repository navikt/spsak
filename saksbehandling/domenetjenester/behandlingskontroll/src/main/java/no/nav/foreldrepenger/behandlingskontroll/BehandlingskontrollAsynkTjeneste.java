package no.nav.foreldrepenger.behandlingskontroll;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

@ApplicationScoped
public class BehandlingskontrollAsynkTjeneste {

    private ProsessTaskRepository prosessTaskRepository;
    private FagsakProsessTaskRepository fagsakProsessTaskRepository;

    BehandlingskontrollAsynkTjeneste() {}

    @Inject
    public BehandlingskontrollAsynkTjeneste(ProsessTaskRepository prosessTaskRepository, FagsakProsessTaskRepository fagsakProsessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.fagsakProsessTaskRepository = fagsakProsessTaskRepository;
    }

    public Map<String, ProsessTaskData> sjekkProsessTaskPågår(Long fagsakId, Long behandlingId, String gruppe) {

        Map<String, List<ProsessTaskData>> statusProsessTasks = sjekkStatusProsessTasksGrouped(fagsakId, behandlingId, gruppe);

        Map<String, ProsessTaskData> nestePerGruppe = nesteProsessTaskPerGruppe(statusProsessTasks);

        if (angittGruppeErFerdig(gruppe, nestePerGruppe)) {
            nestePerGruppe = nesteProsessTaskPerGruppe(sjekkStatusProsessTasksGrouped(fagsakId, behandlingId, null));
        }

        return nestePerGruppe;
    }

    /**
         * Sjekker om prosess tasks pågår nå for angitt behandling. Returnerer neste utestående tasks per gruppe for status (KLAR, FEILET, VENTER),
         * men ikke FERDIG, SUSPENDERT (unntatt der matcher angitt gruppe)
         *
         * Hvis gruppe angis sjekkes kun angitt gruppe. Dersom denne er null returneres status for alle åpne grupper (ikke-ferdig) for angitt
         * behandling. Tasks som er {@link ProsessTaskStatus#FERDIG} ignoreres i resultatet når gruppe ikke er angitt.
         */
    public Map<String, ProsessTaskData> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe) {
        return sjekkProsessTaskPågår(behandling.getFagsakId(), behandling.getId(), gruppe);
    }

    /**
         * Merge ny gruppe med eksisterende, hvis tidligere gruppe er i gang ignoreres input gruppe her. Hvis tidligere gruppe har feil, overskrives
         * denne (ny skrives, gamle fjernes). For å merge sees det på individuelle tasks inne i gruppen (da gruppe id kan være forskjellig uansett).
         */
    public String lagreNyGruppeKunHvisIkkeAlleredeFinnesOgIngenHarFeilet(Long fagsakId, Long behandlingId, ProsessTaskGruppe gruppe) {
        return fagsakProsessTaskRepository.lagreNyGruppeKunHvisIkkeAlleredeFinnesOgIngenHarFeilet(fagsakId, behandlingId, gruppe);
    }

    private Map<String, ProsessTaskData> nesteProsessTaskPerGruppe(Map<String, List<ProsessTaskData>> tasks) {
        // velg top task per gruppe
        Map<String, ProsessTaskData> topTaskPerGruppe = tasks.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty())
            .map(e -> e.getValue()
                .stream()
                .sorted(
                    Comparator.comparing(ProsessTaskData::getSekvens)
                        .thenComparing(Comparator.comparing(ProsessTaskData::getStatus).reversed()) /* NB: avhenger av enum ordinal! */)
                .findFirst().get())
            .collect(Collectors.toMap(ProsessTaskData::getGruppe, Function.identity()));

        return topTaskPerGruppe;
    }

    private boolean angittGruppeErFerdig(String gruppe, Map<String, ProsessTaskData> nestePerGruppe) {
        return gruppe != null
            && (nestePerGruppe.isEmpty()
                || (nestePerGruppe.size() == 1
                    && nestePerGruppe.containsKey(gruppe)
                    && ProsessTaskStatus.FERDIG.equals(nestePerGruppe.get(gruppe).getStatus())));
    }

    private Map<String, List<ProsessTaskData>> sjekkStatusProsessTasksGrouped(Long fagsakId, Long behandlingId, String gruppe) {
        List<ProsessTaskData> tasks = fagsakProsessTaskRepository.sjekkStatusProsessTasks(fagsakId, behandlingId, gruppe);
        return tasks.stream().collect(Collectors.groupingBy(ProsessTaskData::getGruppe));
    }

    /**
         * Kjør prosess asynkront (i egen prosess task) videre.
         *
         * @return gruppe assignet til prosess task
         */
    public String asynkProsesserBehandling(Behandling behandling) {
        ProsessTaskData taskData = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskData.setCallIdFraEksisterende();
        String gruppe = prosessTaskRepository.lagre(taskData);
        return gruppe;
    }

}
