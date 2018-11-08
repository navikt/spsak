package no.nav.foreldrepenger.dokumentbestiller.forlengelsesbrev.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class SendForlengelsesbrevTaskTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final LogSniffer logSniffer = new LogSniffer();

    private SendForlengelsesbrevTask sendForlengelsesbrevTask;

    @Mock
    private DokumentDataTjeneste dokumentDataTjeneste;

    @Mock
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    @Mock
    private ProsessTaskData prosessTaskData;

    @Mock
    private BehandlingRepository behandlingRepository;

    @Mock
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Mock
    private BehandlingskontrollKontekst behandlingskontrollKontekst;

    private ScenarioMorSøkerEngangsstønad scenario;

    @Before
    public void setUp() {
        sendForlengelsesbrevTask = new SendForlengelsesbrevTask(dokumentDataTjeneste, dokumentBestillerApplikasjonTjeneste,
            behandlingRepository, behandlingskontrollTjeneste);
        when(behandlingskontrollTjeneste.initBehandlingskontroll(any(Long.class))).thenReturn(behandlingskontrollKontekst);
        scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
    }

    @Test
    public void testSkalSendeBrevOgOppdatereBehandling() {
        // Arrange
        Behandling behandling = scenario.medBehandlingstidFrist(LocalDate.now().minusDays(1)).lagMocked();
        when(prosessTaskData.getBehandlingId()).thenReturn(behandling.getId());
        when(behandlingRepository.hentBehandling(behandling.getId())).thenReturn(behandling);
        assertThat(behandling.getBehandlingstidFrist()).isBefore(LocalDate.now());

        // Act
        sendForlengelsesbrevTask.doTask(prosessTaskData);

        // Assert
        verify(dokumentBestillerApplikasjonTjeneste).produserDokument(any(), eq(HistorikkAktør.VEDTAKSLØSNINGEN), any());

        ArgumentCaptor<Behandling> behandlingCaptor = ArgumentCaptor.forClass(Behandling.class);
        ArgumentCaptor<BehandlingLås> behandlingLåsCaptor = ArgumentCaptor.forClass(BehandlingLås.class);

        verify(behandlingRepository).lagre(behandlingCaptor.capture(), behandlingLåsCaptor.capture());

        assertThat(behandlingCaptor.getValue().getBehandlingstidFrist()).isAfter(LocalDate.now());

        logSniffer.assertHasInfoMessage("Utført for behandling: " + behandling.getId());
    }

    @Test
    public void testSkalIkkeSendeBrevOgIkkeOppdatereBehandling() {
        // Arrange
        Behandling behandling = scenario.medBehandlingstidFrist(LocalDate.now().plusDays(1)).lagMocked();
        when(prosessTaskData.getBehandlingId()).thenReturn(behandling.getId());
        when(behandlingRepository.hentBehandling(behandling.getId())).thenReturn(behandling);
        assertThat(behandling.getBehandlingstidFrist()).isAfter(LocalDate.now());

        // Act
        sendForlengelsesbrevTask.doTask(prosessTaskData);

        // Assert
        verify(dokumentBestillerApplikasjonTjeneste, never()).produserDokument(anyLong(), any(), anyString());
        assertThat(behandling.getBehandlingstidFrist()).isAfter(LocalDate.now());
        verify(behandlingRepository, never()).lagre(eq(behandling), any());
        logSniffer.assertHasInfoMessage("Ikke utført for behandling: " + behandling.getId());
    }
}
