package no.nav.foreldrepenger.behandling.statusobserver.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchStatus;
import no.nav.foreldrepenger.batch.EmptyBatchArguments;
import no.nav.foreldrepenger.behandling.statusobserver.AutomatiskFagsakAvslutningTjeneste;
import no.nav.foreldrepenger.behandling.statusobserver.OppdaterFagsakStatusFelles;
import no.nav.foreldrepenger.behandling.statusobserver.task.AutomatiskFagsakAvslutningBatchTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

public class AutomatiskFagsakAvslutningBatchTjenesteTest {

    private AutomatiskFagsakAvslutningBatchTjeneste tjeneste;
    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private FagsakRepository fagsakRepository;
    private OppdaterFagsakStatusFelles oppdaterFagsakStatusFelles;


    @Before
    public void setUp() throws Exception {
        behandlingRepository = Mockito.mock(BehandlingRepository.class);
        prosessTaskRepository = Mockito.mock(ProsessTaskRepository.class);
        fagsakRepository = Mockito.mock(FagsakRepository.class);
        oppdaterFagsakStatusFelles = Mockito.mock(OppdaterFagsakStatusFelles.class);
        AutomatiskFagsakAvslutningTjeneste fagsakAvslutningTjeneste = new AutomatiskFagsakAvslutningTjeneste(behandlingRepository,
            prosessTaskRepository, fagsakRepository, oppdaterFagsakStatusFelles);
        tjeneste = new AutomatiskFagsakAvslutningBatchTjeneste(fagsakAvslutningTjeneste);
    }

    @Test
    public void skal_returnere_status_ok_ved_fullført() throws Exception {
        final List<TaskStatus> statuses = Collections.singletonList(new TaskStatus(ProsessTaskStatus.FERDIG, BigDecimal.ONE));
        Mockito.when(prosessTaskRepository.finnStatusForTaskIGruppe(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(statuses);

        final BatchStatus status = tjeneste.status("1234");

        Mockito.verify(prosessTaskRepository).finnStatusForTaskIGruppe("behandlingskontroll.fagsakAvslutning", "1234");
        assertThat(status).isEqualTo(BatchStatus.OK);
    }

    @Test
    public void skal_kjøre_batch_uten_feil() throws Exception {
        final BatchArguments batchArguments = new EmptyBatchArguments(new HashMap<>());
        Fagsak fagsak1 = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, null);
        fagsak1.setId(1L);
        Fagsak fagsak2 = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, null);
        fagsak2.setId(2L);
        Behandling behandling = Mockito.mock(Behandling.class);
        Mockito.when(behandling.getId()).thenReturn(3L);
        Mockito.when(behandling.getFagsakId()).thenReturn(2L);
        Mockito.when(behandling.getAktørId()).thenReturn(new AktørId(4L));
        Mockito.when(fagsakRepository.hentForStatus(FagsakStatus.LØPENDE)).thenReturn(Arrays.asList(fagsak1, fagsak2));
        Mockito.when(behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(1L)).thenReturn(Optional.empty());
        Mockito.when(behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(2L)).thenReturn(Optional.of(behandling));
        Mockito.when(oppdaterFagsakStatusFelles.ingenLøpendeYtelsesvedtak(behandling)).thenReturn(true);

        final String batchId = tjeneste.launch(batchArguments);

        Mockito.verify(fagsakRepository, Mockito.times(1)).hentForStatus(FagsakStatus.LØPENDE);
        Mockito.verify(behandlingRepository, Mockito.times(2)).finnSisteAvsluttedeIkkeHenlagteBehandling(ArgumentMatchers.anyLong());
        Mockito.verify(oppdaterFagsakStatusFelles, Mockito.times(1)).ingenLøpendeYtelsesvedtak(behandling);
        Mockito.verify(prosessTaskRepository).lagre(ArgumentMatchers.any(ProsessTaskData.class));
        Assertions.assertThat(batchId.substring(0, 6)).isEqualTo("BVL006");
    }
}
