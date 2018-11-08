package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.inject.Instance;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InnhentRelaterteYtelserTaskTest {
    private static final String FNR_SØKER = "01020398765";
    private static final AktørId AKTØRID_ANNEN = new AktørId("987");
    private static final String PERIODE = "P10M";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private InnhentRelaterteYtelserTask task;
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;

    @Mock
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;

    @Mock
    private BehandlingLås lås;
    @Mock
    private RegisterdataInnhenter registerdataInnhenter;
    @Mock
    private Instance<String> periodeInstance;
    @Mock
    private Personinfo personinfo;

    private void setupMocks() {
        prosessTaskRepository = mock(ProsessTaskRepository.class);
        behandlingskontrollTaskTjeneste = new BehandlingskontrollTaskTjenesteImpl(prosessTaskRepository);
        when(behandlingskontrollTjeneste.initBehandlingskontroll(Mockito.anyLong())).thenReturn(new BehandlingskontrollKontekst(null, null, lås));
        when(periodeInstance.get()).thenReturn(PERIODE);

        task = new TestInnhentRelaterteYtelserTask(repositoryProvider, behandlingskontrollTjeneste, behandlingskontrollTaskTjeneste, registerdataInnhenter);
        when(personinfo.getPersonIdent()).thenReturn(new PersonIdent(FNR_SØKER));
        when(registerdataInnhenter.innhentSaksopplysningerForSøker(any())).thenReturn(personinfo);
    }

    @Test
    public void innhentTaskForBareSøker() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        Behandling behandling = scenario.lagMocked();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        setupMocks();

        ProsessTaskData prosessTask = new ProsessTaskData(InnhentRelaterteYtelserTask.TASKTYPE);
        prosessTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTask.setSekvens("101");

        // Act
        task.doTask(prosessTask);

        // Assert
        verify(behandlingRepository).lagre(behandling, lås);
        verify(prosessTaskRepository).lagre(any(ProsessTaskData.class));
    }

    @Test
    public void innhentTaskForSøkerOgAnnenForelder() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        scenario.medSøknadAnnenPart().medAktørId(AKTØRID_ANNEN);
        final Behandling behandling = scenario.lagMocked();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        setupMocks();

        ProsessTaskData prosessTask = new ProsessTaskData(InnhentRelaterteYtelserTask.TASKTYPE);
        prosessTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTask.setSekvens("101");

        when(registerdataInnhenter.innhentSaksopplysningerForMedSøker(any())).thenReturn(Optional.of(personinfo));

        // Act
        task.doTask(prosessTask);

        // Assert
        verify(behandlingRepository).lagre(behandling, lås);
        verify(prosessTaskRepository).lagre(any(ProsessTaskData.class));
    }

    private class TestInnhentRelaterteYtelserTask extends InnhentRelaterteYtelserTask {

        public TestInnhentRelaterteYtelserTask(BehandlingRepositoryProvider bepositoryProvider, BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                               BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste, RegisterdataInnhenter registerdataInnhenter) {
            super(bepositoryProvider, behandlingskontrollTjeneste, behandlingskontrollTaskTjeneste, registerdataInnhenter);
        }
    }
}
