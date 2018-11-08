package no.nav.vedtak.felles.prosesstask.rest.app;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.felles.prosesstask.rest.dto.FeiletProsessTaskDataDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskDataDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskDataKonverter;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskDataPayloadDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskRestartInputDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskRestartResultatDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskRetryAllResultatDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskStatusDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.SokeFilterDto;
import no.nav.vedtak.felles.prosesstask.rest.feil.ProsessTaskRestTjenesteFeil;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class ProsessTaskApplikasjonTjenesteImpl implements ProsessTaskApplikasjonTjeneste {

    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    public ProsessTaskApplikasjonTjenesteImpl(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public List<ProsessTaskDataDto> finnAlle(SokeFilterDto sokeFilterDto) {

        // Hvis ikke spesifisert, søkes det default bare på ProsessTaskStatus.KLAR og ProsessTaskStatus.VENTER_SVAR
        if (sokeFilterDto.getProsessTaskStatuser().isEmpty()) {
            sokeFilterDto.getProsessTaskStatuser().add(new ProsessTaskStatusDto(ProsessTaskStatus.KLAR.getDbKode()));
            sokeFilterDto.getProsessTaskStatuser().add(new ProsessTaskStatusDto(ProsessTaskStatus.VENTER_SVAR.getDbKode()));
        }

        if (sokeFilterDto.getSisteKjoeretidspunktFraOgMed().isAfter(sokeFilterDto.getSisteKjoeretidspunktTilOgMed())) {
            return Collections.emptyList();
        }

        List<ProsessTaskStatus> statuser = sokeFilterDto.getProsessTaskStatuser().stream().map(e -> ProsessTaskStatus.valueOf(e.getProsessTaskStatusName())).collect(Collectors.toList());
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(statuser, sokeFilterDto.getSisteKjoeretidspunktFraOgMed(), sokeFilterDto.getSisteKjoeretidspunktTilOgMed());
        prosessTaskData.addAll(prosessTaskRepository.finnIkkeStartet());
        return prosessTaskData.stream().map(ProsessTaskDataKonverter::tilProsessTaskDataDto).collect(Collectors.toList());
    }

    @Override
    public Optional<FeiletProsessTaskDataDto> finnFeiletProsessTask(Long prosessTaskId) {
        ProsessTaskData taskData = prosessTaskRepository.finn(prosessTaskId);
        if (taskData != null && taskData.getStatus().equals(ProsessTaskStatus.FEILET)) {
            return Optional.of(ProsessTaskDataKonverter.tilFeiletProsessTaskDataDto(taskData));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ProsessTaskDataPayloadDto> finnProsessTaskMedPayload(Long prosessTaskId) {
        ProsessTaskData taskData = prosessTaskRepository.finn(prosessTaskId);
        return taskData == null ? Optional.empty() : Optional.of(ProsessTaskDataKonverter.tilProsessTaskDataPayloadDto(taskData));
    }

    @Override
    public ProsessTaskRestartResultatDto flaggProsessTaskForRestart(ProsessTaskRestartInputDto prosessTaskRestartInputDto) {
        ProsessTaskData ptd = prosessTaskRepository.finn(prosessTaskRestartInputDto.getProsessTaskId());

        validerBetingelserForRestart(prosessTaskRestartInputDto.getProsessTaskId(), prosessTaskRestartInputDto.getNaaVaaerendeStatus(), ptd);

        oppdaterProsessTaskDataMedKjoerbarStatus(ptd);
        prosessTaskRepository.lagre(ptd);

        ProsessTaskRestartResultatDto restartResultatDto = new ProsessTaskRestartResultatDto();
        restartResultatDto.setNesteKjoeretidspunkt(ptd.getNesteKjøringEtter());
        restartResultatDto.setProsessTaskId(prosessTaskRestartInputDto.getProsessTaskId());
        restartResultatDto.setProsessTaskStatus(ProsessTaskStatus.KLAR.getDbKode());
        return restartResultatDto;
    }

    @Override
    public ProsessTaskRetryAllResultatDto flaggAlleFeileteProsessTasksForRestart() {
        ProsessTaskRetryAllResultatDto retryAllResultatDto = new ProsessTaskRetryAllResultatDto();

        List<ProsessTaskData> ptdList = prosessTaskRepository.finnAlle(ProsessTaskStatus.FEILET);

        Map<String, Integer> taskTypesMaxForsøk = new HashMap<>();
        ptdList.stream().map(ProsessTaskData::getTaskType).forEach(tasktype -> {
            if (taskTypesMaxForsøk.get(tasktype) == null) {
                int forsøk = prosessTaskRepository.finnProsessTaskType(tasktype).map(ProsessTaskTypeInfo::getMaksForsøk).orElse(1);
                taskTypesMaxForsøk.put(tasktype, forsøk);
            }
        });
        LocalDateTime naa = LocalDateTime.now(FPDateUtil.getOffset());
        ptdList.forEach(ptd -> {
            ptd.setStatus(ProsessTaskStatus.KLAR);
            ptd.setNesteKjøringEtter(naa);
            ptd.setSisteFeilKode(null);
            ptd.setSisteFeil(null);
            if (taskTypesMaxForsøk.get(ptd.getTaskType()) == ptd.getAntallFeiledeForsøk()) { // NOSONAR
                ptd.setAntallFeiledeForsøk(ptd.getAntallFeiledeForsøk() - 1);
            }
            prosessTaskRepository.lagre(ptd);
            retryAllResultatDto.addProsessTaskId(ptd.getId());
        });
        return retryAllResultatDto;
    }

    private void oppdaterProsessTaskDataMedKjoerbarStatus(ProsessTaskData eksisterendeProsessTaskData) {

        eksisterendeProsessTaskData.setStatus(ProsessTaskStatus.KLAR);
        eksisterendeProsessTaskData.setNesteKjøringEtter(LocalDateTime.now(FPDateUtil.getOffset()));
        eksisterendeProsessTaskData.setSisteFeilKode(null);
        eksisterendeProsessTaskData.setSisteFeil(null);

        /**
         * Tvungen kjøring: reduserer anall feilede kjøring med 1 slik at {@link no.nav.foreldrepenger.felles.prosesstask.impl.TaskManager}
         * kan plukke den opp og kjøre.
         */
        Optional<ProsessTaskTypeInfo> taskTypeInfo = prosessTaskRepository.finnProsessTaskType(eksisterendeProsessTaskData.getTaskType());
        if (taskTypeInfo.get().getMaksForsøk() == eksisterendeProsessTaskData.getAntallFeiledeForsøk()) { // NOSONAR
            eksisterendeProsessTaskData.setAntallFeiledeForsøk(eksisterendeProsessTaskData.getAntallFeiledeForsøk() - 1);
        }
    }

    private void validerBetingelserForRestart(Long prosessTaskId, String nåværendeStatus, ProsessTaskData ptd) {
        if (ptd != null) {
            if (ptd.getStatus().equals(ProsessTaskStatus.FERDIG)) {
                throw ProsessTaskRestTjenesteFeil.FACTORY.kanIkkeRestarteEnFerdigKjørtProsesstask(prosessTaskId).toException();
            }
            if (!ProsessTaskStatus.KLAR.equals(ptd.getStatus()) && (nåværendeStatus == null || !ptd.getStatus().equals(ProsessTaskStatus.valueOf(nåværendeStatus)))) {
                throw ProsessTaskRestTjenesteFeil.FACTORY.måAngiNåværendeProsesstaskStatusForRestart(prosessTaskId).toException();
            }
        } else {
            throw ProsessTaskRestTjenesteFeil.FACTORY.ukjentProsessTaskIdAngitt(prosessTaskId).toException();
        }
    }

}
