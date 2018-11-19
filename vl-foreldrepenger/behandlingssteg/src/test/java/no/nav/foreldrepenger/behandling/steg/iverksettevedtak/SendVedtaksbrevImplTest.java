package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;

public class SendVedtaksbrevImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Mock
    private DokumentData dokumentData;

    @Mock
    private Behandlingsresultat behandlingsresultat;

    private SendVedtaksbrev sendVedtaksbrev;

    private Behandling behandling;
    private BehandlingVedtak behandlingVedtak;

    @Mock
    private KlageVurderingResultat klageVurderingResultat;

    private BehandlingRepositoryProvider repositoryProvider;

    @Before
    public void oppsett() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        behandling = scenario.lagMocked();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        behandlingVedtak = scenario.mockBehandlingVedtak();
        sendVedtaksbrev = new SendVedtaksbrevImpl(repositoryProvider, mock(SendVarselTjeneste.class));
    }

    @Test
    public void testSendVedtaksbrevVedtakInnvilget() {
        // Arrange
        when(behandlingVedtak.getVedtakResultatType()).thenReturn(VedtakResultatType.INNVILGET);

        // Act
        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

        // Assert
    }

    @Test
    public void testSendVedtaksbrevVedtakAvslag() {
        // Arrange
        when(behandlingVedtak.getVedtakResultatType()).thenReturn(VedtakResultatType.AVSLAG);

        // Act
        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

        // Assert
    }

}
