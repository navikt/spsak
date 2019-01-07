package no.nav.vedtak.felles.prosesstask.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class ProsessTaskRepositoryImplIT {

    private static final LocalDateTime NÅ = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private ProsessTaskRepository prosessTaskRepository;

    private final LocalDateTime nesteKjøringEtter = NÅ.plusHours(1);
    
    @Before
    public void setUp() throws Exception {
        ProsessTaskEventPubliserer prosessTaskEventPubliserer = Mockito.mock(ProsessTaskEventPubliserer.class);
        Mockito.doNothing().when(prosessTaskEventPubliserer).fireEvent(Mockito.any(ProsessTaskData.class), Mockito.any(), Mockito.any(), Mockito.any());
        prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), prosessTaskEventPubliserer);

        lagTestData();
    }

    @Test
    public void test_ingen_match_innenfor_et_kjøretidsintervall() throws Exception {
        List<ProsessTaskStatus> statuser = Arrays.asList(ProsessTaskStatus.values());
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(statuser, NÅ.minusHours(1), NÅ);

        Assertions.assertThat(prosessTaskData).isEmpty();
    }

    @Test
    public void test_har_match_innenfor_et_kjøretidsntervall() throws Exception {
        List<ProsessTaskStatus> statuser = Arrays.asList(ProsessTaskStatus.values());
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(statuser, NÅ.minusHours(2), NÅ);

        Assertions.assertThat(prosessTaskData).hasSize(1);
        Assertions.assertThat(prosessTaskData.get(0).getStatus()).isEqualTo(ProsessTaskStatus.FERDIG);
    }

    @Test
    public void test_ingen_match_for_angitt_prosesstatus() throws Exception {
        List<ProsessTaskStatus> statuser = Arrays.asList(ProsessTaskStatus.SUSPENDERT);
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(statuser, NÅ.minusHours(2), NÅ);

        Assertions.assertThat(prosessTaskData).isEmpty();
    }
    
    @Test
    public void test_skal_finne_tasks_som_matcher_angitt_søk() throws Exception {
        
        List<ProsessTaskStatus> statuser = Arrays.asList(ProsessTaskStatus.SUSPENDERT);
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlleForAngittSøk(statuser, null, nesteKjøringEtter, nesteKjøringEtter, "fagsakId=1%behandlingId=2%");

        Assertions.assertThat(prosessTaskData).hasSize(1);
    }

    private void lagTestData() {
        ProsessTaskType taskType = new ProsessTaskType("hello.world");
        repository.lagre(taskType);
        repository.flushAndClear();
        
        repository.lagre(lagTestEntitet(ProsessTaskStatus.FERDIG, NÅ.minusHours(2)));
        repository.lagre(lagTestEntitet(ProsessTaskStatus.VENTER_SVAR, NÅ.minusHours(3)));
        repository.lagre(lagTestEntitet(ProsessTaskStatus.FEILET, NÅ.minusHours(4)));
        repository.lagre(lagTestEntitet(ProsessTaskStatus.KLAR, NÅ.minusHours(5)));
        repository.lagre(lagTestEntitet(ProsessTaskStatus.SUSPENDERT, NÅ.minusHours(6)));
        repository.flushAndClear();
    }

    private ProsessTaskEntitet lagTestEntitet(ProsessTaskStatus status, LocalDateTime sistKjørt) {
        ProsessTaskData data = new ProsessTaskData("hello.world");
        data.setPayload("payload");
        data.setStatus(status);
        data.setSisteKjøringServerProsess("prossess-123");
        data.setSisteFeilKode("feilkode-123");
        data.setSisteFeil("siste-feil");
        data.setAntallFeiledeForsøk(2);
        data.setBehandling(1L, 2L, "3");
        data.setGruppe("gruppe");
        data.setNesteKjøringEtter(nesteKjøringEtter);
        data.setPrioritet(2);
        data.setSekvens("123");

        if (sistKjørt != null) {
            data.setSistKjørt(sistKjørt);
        }

        ProsessTaskEntitet pte = new ProsessTaskEntitet();
        return pte.kopierFra(data);
    }

}
