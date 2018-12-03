package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Period;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndterMottattDokumentTaskProperties;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEventPubliserer;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

@SuppressWarnings("deprecation")
public class OpprettNyFørstegangsbehandlingTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private Behandling behandling;

    private BehandlingRepositoryProvider repositoryProvider;

    private OppgaveTjeneste oppgaveTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private SaksbehandlingDokumentmottakTjeneste saksbehandlingDokumentmottakTjeneste;
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    private KodeverkRepository kodeverkRepository;

    private Behandling opprettOgLagreBehandling() {
        return ScenarioMorSøkerEngangsstønad.forDefaultAktør().lagre(repositoryProvider);
    }

    @Before
    public void setup() {
        ProsessTaskEventPubliserer prosessTaskEventPubliserer = Mockito.mock(ProsessTaskEventPubliserer.class);
        Mockito.doNothing().when(prosessTaskEventPubliserer).fireEvent(Mockito.any(ProsessTaskData.class), Mockito.any(), Mockito.any(), Mockito.any());
        oppgaveTjeneste = mock(OppgaveTjeneste.class);
        prosessTaskRepository = Mockito.spy(new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), prosessTaskEventPubliserer));

        repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(repoRule.getEntityManager()));
        kodeverkRepository = repositoryProvider.getKodeverkRepository();
        behandling = opprettOgLagreBehandling();

        saksbehandlingDokumentmottakTjeneste = new SaksbehandlingDokumentmottakTjenesteImpl(prosessTaskRepository);

        behandlingsutredningApplikasjonTjeneste = new BehandlingsutredningApplikasjonTjenesteImpl(
            Period.parse("P4W"),
            repositoryProvider,
            null,
            oppgaveTjeneste,
            null,
            null,
            saksbehandlingDokumentmottakTjeneste);
    }

    @Test
    public void skal_opprette_nyførstegangsbehandling() {
        //Arrange
        behandling.avsluttBehandling();

        //Act
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(behandling.getFagsakId(), behandling.getFagsak().getSaksnummer());

        //Assert
        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskRepository, times(1)).lagre(captor.capture());
        ProsessTaskData prosessTaskData = captor.getValue();
        verifiserProsessTaskData(behandling, prosessTaskData);

    }

    @Test(expected = FunksjonellException.class)
    public void skal_kaste_exception_når_behandling_fortsatt_er_åpen() {
        //Act and expect Exception
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(behandling.getFagsakId(), behandling.getFagsak().getSaksnummer());
    }

    @Test(expected = FunksjonellException.class)
    public void skal_kaste_exception_når_behandling_ikke_eksisterer() {
        //Act and expect Exception
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(-1L, new Saksnummer("50"));
    }

    //Verifiserer at den opprettede prosesstasken stemmer overens med MottattDokument-mock
    private void verifiserProsessTaskData(Behandling behandling, ProsessTaskData prosessTaskData) {
        final BehandlingTema behandlingTemaFødsel = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.SYKEPENGER);

        assertThat(prosessTaskData.getTaskType()).isEqualTo(HåndterMottattDokumentTaskProperties.TASKTYPE);
        assertThat(prosessTaskData.getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(prosessTaskData.getPropertyValue(HåndterMottattDokumentTaskProperties.BEHANDLINGSTEMA_OFFISIELL_KODE_KEY))
            .isEqualTo(behandlingTemaFødsel.getOffisiellKode());
    }
}
