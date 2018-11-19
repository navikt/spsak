package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;

public class SendVedtaksbrevForeldrepengerTest {

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

    private BehandlingRepository behandlingRepository;

    private BehandlingRepositoryProvider repositoryProvider;

    @Before
    public void oppsett() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagMocked();
        behandlingRepository = scenario.mockBehandlingRepository();
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

    }

    @Test
    public void testSendVedtaksbrevVedtakAvslag() {
        // Arrange
        when(behandlingVedtak.getVedtakResultatType()).thenReturn(VedtakResultatType.AVSLAG);

        // Act
        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

    }

    @Test
    public void testSendVedtaksbrevEtterKlagebehandlingMedholdNFP() {
        testSendVedtaksbrevEtterKlagebehandling(BehandlingResultatType.KLAGE_MEDHOLD, KlageVurdering.MEDHOLD_I_KLAGE, KlageVurdertAv.NFP,
                false);
    }

    @Test
    public void testSendVedtaksbrevEtterKlagebehandlingMedholdNK() {
        testSendVedtaksbrevEtterKlagebehandling(BehandlingResultatType.KLAGE_MEDHOLD, KlageVurdering.MEDHOLD_I_KLAGE, KlageVurdertAv.NK,
                false);
    }

    private void testSendVedtaksbrevEtterKlagebehandling(BehandlingResultatType behandlingResultat, KlageVurdering klageVurdering,
            KlageVurdertAv klageVurdertAv, boolean skalSende) {
        // Arrange
        // TODO (ONYX) erstatt med scenario
        Behandling behandlingMock = Mockito.mock(Behandling.class);
        when(behandlingMock.erKlage()).thenReturn(true);
        when(behandlingMock.hentGjeldendeKlageVurderingResultat()).thenReturn(Optional.of(klageVurderingResultat));
        Fagsak fagsakMock = Mockito.mock(Fagsak.class);
        when(fagsakMock.getYtelseType()).thenReturn(FagsakYtelseType.FORELDREPENGER);
        when(behandlingMock.getFagsak()).thenReturn(fagsakMock);
        when(behandlingVedtak.getVedtakResultatType()).thenReturn(VedtakResultatType.VEDTAK_I_KLAGEBEHANDLING);
        when(behandlingRepository.hentBehandling(behandling.getId())).thenReturn(behandlingMock);
        when(klageVurderingResultat.getKlageVurdering()).thenReturn(klageVurdering);
        when(klageVurderingResultat.getKlageVurdering()).thenReturn(klageVurdering);
        when(klageVurderingResultat.getKlageVurdertAv()).thenReturn(klageVurdertAv);
        when(behandlingsresultat.getBehandlingResultatType()).thenReturn(behandlingResultat);
        when(behandlingVedtak.getBehandlingsresultat()).thenReturn(behandlingsresultat);

        // Act
        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

    }

    @Test
    public void senderBrevOmUendretUtfallVedRevurdering() {
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(true);
    }

    @Test
    public void senderIkkeBrevOmUendretUtfallHvisIkkeSendtVarselbrevOmRevurdering() {
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(true);
    }
}
