package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import static org.assertj.core.api.Assertions.assertThat;
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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndterMottattDokumentTaskProperties;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEventPubliserer;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

@SuppressWarnings("deprecation")
public class OpprettNyFørstegangsbehandlingTest {

    private final AbstractTestScenario<?> scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Behandling behandling;
    private GrunnlagRepositoryProvider repositoryProvider;
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repoRule.getEntityManager());
    private ProsessTaskRepository prosessTaskRepository;
    private SaksbehandlingDokumentmottakTjeneste saksbehandlingDokumentmottakTjeneste;
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    private KodeverkRepository kodeverkRepository;

    private Behandling opprettOgLagreBehandling() {
        return scenario.lagre(repositoryProvider, resultatRepositoryProvider);
    }

    @Before
    public void setup() {
        ProsessTaskEventPubliserer prosessTaskEventPubliserer = Mockito.mock(ProsessTaskEventPubliserer.class);
        Mockito.doNothing().when(prosessTaskEventPubliserer).fireEvent(Mockito.any(ProsessTaskData.class), Mockito.any(), Mockito.any(), Mockito.any());
        prosessTaskRepository = Mockito.spy(new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), prosessTaskEventPubliserer));

        repositoryProvider = Mockito.spy(new GrunnlagRepositoryProviderImpl(repoRule.getEntityManager()));
        kodeverkRepository = repositoryProvider.getKodeverkRepository();
        behandling = opprettOgLagreBehandling();

        saksbehandlingDokumentmottakTjeneste = new SaksbehandlingDokumentmottakTjenesteImpl(prosessTaskRepository);

        behandlingsutredningApplikasjonTjeneste = new BehandlingsutredningApplikasjonTjenesteImpl(
            Period.parse("P4W"),
            repositoryProvider,
            null,
            null,
            null,
            saksbehandlingDokumentmottakTjeneste);
    }

    @Test
    public void skal_opprette_nyførstegangsbehandling() {
        //Arrange
        scenario.avsluttBehandling(repositoryProvider, behandling);

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
