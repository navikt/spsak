package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLås;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class OpprettOppgaveVurderDokumentTaskTest {

    private static final long FAGSAK_ID = 2L;

    private OppgaveTjeneste oppgaveTjeneste;
    private BehandlingRepositoryProvider repositoryProvider;
    private OpprettOppgaveVurderDokumentTask opprettOppgaveVurderDokumentTask;
    private FagsakLåsRepository låsRepository;

    private KodeverkRepository kodeverkRepository;

    @Before
    public void before() {
        oppgaveTjeneste = mock(OppgaveTjeneste.class);
        repositoryProvider = mock(BehandlingRepositoryProvider.class);
        låsRepository = mock(FagsakLåsRepository.class);
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();

        when(repositoryProvider.getFagsakLåsRepository()).thenReturn(låsRepository);
        when(låsRepository.taLås(anyLong())).thenReturn(mock(FagsakLås.class));

        opprettOppgaveVurderDokumentTask = new OpprettOppgaveVurderDokumentTask(oppgaveTjeneste, kodeverkRepository, repositoryProvider);
    }

    @Test
    public void skal_opprette_oppgave_for_å_vurdere_dokument_basert_på_fagsakId() {
        // Arrange
        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettOppgaveVurderDokumentTask.TASKTYPE);
        prosessTaskData.setFagsakId(FAGSAK_ID);
        prosessTaskData.setProperty(OpprettOppgaveVurderDokumentTask.KEY_DOKUMENT_TYPE, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode());
        ArgumentCaptor<Long> fagsakIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<OppgaveÅrsak> årsakCaptor = ArgumentCaptor.forClass(OppgaveÅrsak.class);
        ArgumentCaptor<String> fordelingsoppgaveEnhetsIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> beskrivelseCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> priCaptor = ArgumentCaptor.forClass(Boolean.class);

        // Act
        opprettOppgaveVurderDokumentTask.doTask(prosessTaskData);

        // Assert
        verify(oppgaveTjeneste).opprettMedPrioritetOgBeskrivelseBasertPåFagsakId(fagsakIdCaptor.capture(), årsakCaptor.capture(),
            fordelingsoppgaveEnhetsIdCaptor.capture(), beskrivelseCaptor.capture(), priCaptor.capture());
        assertThat(fagsakIdCaptor.getValue()).isEqualTo(FAGSAK_ID);
        assertThat(årsakCaptor.getValue()).isEqualTo(OppgaveÅrsak.VURDER_DOKUMENT);
        assertThat(beskrivelseCaptor.getValue()).isEqualTo("VL: " + DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode()); // Antar testhelper, ellers bruk finn+navn
    }
}
