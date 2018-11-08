package no.nav.foreldrepenger.behandling.innsyn.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class InnsynTjenesteImplTest {

    @Mock
    private BehandlingskontrollTjeneste behandlingKontrollTjeneste;
    @Mock
    private HistorikkRepository historikkRepository;
    @Mock
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;

    @Before
    public void oppsett() {
        initMocks(this);
        // Initierer mockene som ikke er avhengig av scenario-oppsettet

        when(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(any(Behandling.class))).thenReturn(new OrganisasjonsEnhet("1234", ""));
    }

    @Test
    public void skal_opprette_innsyn_på_fagsak() {
        // Arrange
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling opprinneligBehandling = scenario.lagMocked();
        Saksnummer saksnummer = opprinneligBehandling.getFagsak().getSaksnummer();

        // Oppsett av members for tjenesten
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        KodeverkRepositoryImpl kodeverkRepository = Mockito.mock(KodeverkRepositoryImpl.class);
        when(kodeverkRepository.finn(BehandlingType.class, BehandlingType.INNSYN)).thenReturn(BehandlingType.INNSYN);
        InnsynHistorikkTjeneste innsynHistorikkTjeneste = new InnsynHistorikkTjeneste(historikkRepository);

        InnsynTjenesteImpl innsynTjeneste = new InnsynTjenesteImpl(behandlingKontrollTjeneste, innsynHistorikkTjeneste,
                repositoryProvider, behandlendeEnhetTjeneste);

        // Act
        Behandling nyBehandling = innsynTjeneste.opprettManueltInnsyn(saksnummer);

        // Assert
        assertThat(nyBehandling.getType()).isEqualTo(BehandlingType.INNSYN);
    }
}
