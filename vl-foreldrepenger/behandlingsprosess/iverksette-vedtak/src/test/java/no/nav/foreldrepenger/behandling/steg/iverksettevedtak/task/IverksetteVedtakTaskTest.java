package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.AvsluttBehandling;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.SendVedtaksbrev;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.VurderOgSendØkonomiOppdrag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.vedtak.VurderOmArenaYtelseSkalOpphøre;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class IverksetteVedtakTaskTest {

    private static final Long BEHANDLING_ID = 125L;

    private static final Long FAGSAK_ID = 458L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Mock
    private AvsluttBehandling avsluttBehandlingTjeneste;
    @Mock
    private SendVedtaksbrev sendVedtaksbrevTjeneste;
    @Mock
    private VurderOgSendØkonomiOppdrag vurderOgSendØkonomiOppdragTjeneste;
    @Mock
    private VurderOmArenaYtelseSkalOpphøre vurdereOmArenaYtelseSkalOpphøre;

    private final static LocalDateTime NESTE_KJØRING_ETTER = LocalDateTime.now().minusSeconds(5);

    private ProsessTaskData avsluttBehandling = opprettProsessTaskData(AvsluttBehandlingTask.TASKTYPE);
    private ProsessTaskData sendVedtaksbrev = opprettProsessTaskData(SendVedtaksbrevTask.TASKTYPE);
    private ProsessTaskData vurderOgSendØkonomiOppdrag = opprettProsessTaskData(VurderOgSendØkonomiOppdragTask.TASKTYPE);

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingRepositoryProvider repositoryProvider;

    @Before
    public void setup() throws SQLException {
        prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null);
        repositoryProvider = ScenarioMorSøkerForeldrepenger.forFødsel().mockBehandlingRepositoryProvider();
    }

    private ProsessTaskData opprettProsessTaskData(String tasktype) {
        ProsessTaskData ptd = new ProsessTaskData(tasktype);
        ptd.setBehandling(FAGSAK_ID, BEHANDLING_ID, "99");
        ptd.setNesteKjøringEtter(NESTE_KJØRING_ETTER);
        return ptd;
    }

    @Test
    public void testAvsluttBehandling() {
        AvsluttBehandlingTask avsluttBehandlingTask = new AvsluttBehandlingTask(avsluttBehandlingTjeneste, repositoryProvider);
        avsluttBehandlingTask.doTask(avsluttBehandling);
    }

    @Test
    public void testSendVedtaksbrev() {
        SendVedtaksbrevTask sendVedtaksbrevTask = new SendVedtaksbrevTask(sendVedtaksbrevTjeneste, repositoryProvider);
        sendVedtaksbrevTask.doTask(sendVedtaksbrev);
    }

    @Test
    public void testVurderOgSendØkonomiOppdrag() {
        VurderOgSendØkonomiOppdragTask vurderOgSendØkonomiOppdragTask = new VurderOgSendØkonomiOppdragTask(
            vurderOgSendØkonomiOppdragTjeneste, prosessTaskRepository, repositoryProvider);
        vurderOgSendØkonomiOppdragTask.doTask(vurderOgSendØkonomiOppdrag);
    }

    @Test
    public void testLagreTaskGruppe() {
        ProsessTaskGruppe taskData = new ProsessTaskGruppe()
            .addNesteParallell(sendVedtaksbrev, vurderOgSendØkonomiOppdrag)
            .addNesteSekvensiell(avsluttBehandling);
        prosessTaskRepository.lagre(taskData);
    }
}
