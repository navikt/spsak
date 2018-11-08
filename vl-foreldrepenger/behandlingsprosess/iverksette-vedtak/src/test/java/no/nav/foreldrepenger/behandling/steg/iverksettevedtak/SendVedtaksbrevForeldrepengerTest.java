package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

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
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentDataTjeneste;

public class SendVedtaksbrevForeldrepengerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Mock
    private DokumentDataTjeneste dokumentDataTjeneste;
    @Mock
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;
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
        scenario.medSøknadHendelse().medFødselsDato(LocalDate.now());
        behandling = scenario.lagMocked();
        behandlingRepository = scenario.mockBehandlingRepository();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        behandlingVedtak = scenario.mockBehandlingVedtak();
        sendVedtaksbrev = new SendVedtaksbrevImpl(repositoryProvider, dokumentBestillerApplikasjonTjeneste);
    }

    @Test
    public void testSendVedtaksbrevVedtakInnvilget() {
        // Arrange
        when(behandlingVedtak.getVedtakResultatType()).thenReturn(VedtakResultatType.INNVILGET);

        // Act
        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

        verify(dokumentBestillerApplikasjonTjeneste).produserVedtaksbrev(behandlingVedtak);
    }

    @Test
    public void testSendVedtaksbrevVedtakAvslag() {
        // Arrange
        when(behandlingVedtak.getVedtakResultatType()).thenReturn(VedtakResultatType.AVSLAG);

        // Act
        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

        verify(dokumentBestillerApplikasjonTjeneste).produserVedtaksbrev(behandlingVedtak);
    }

    @Test
    public void testSendVedtaksbrevEtterKlagebehandlingAvvistNFP() {
        // TODO Endre skalSendes til true når klagebrev FP er implementert
        testSendVedtaksbrevEtterKlagebehandling(BehandlingResultatType.KLAGE_AVVIST, KlageVurdering.AVVIS_KLAGE, KlageVurdertAv.NFP, false);
    }

    @Test
    public void testSendVedtaksbrevEtterKlagebehandlingAvvistNK() {
        // TODO Endre skalSendes til true når klagebrev FP er implementert
        testSendVedtaksbrevEtterKlagebehandling(BehandlingResultatType.KLAGE_AVVIST, KlageVurdering.AVVIS_KLAGE, KlageVurdertAv.NK, false);
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

    @Test
    public void testSendVedtaksbrevEtterKlagebehandlingOpphevet() {
        // TODO Endre skalSendes til true når klagebrev FP er implementert
        testSendVedtaksbrevEtterKlagebehandling(BehandlingResultatType.KLAGE_YTELSESVEDTAK_OPPHEVET, KlageVurdering.OPPHEVE_YTELSESVEDTAK,
                KlageVurdertAv.NK, false);
    }

    @Test
    public void testSendVedtaksbrevEtterKlagebehandlingStadfestet() {
        // TODO Endre skalSendes til true når klagebrev FP er implementert
        testSendVedtaksbrevEtterKlagebehandling(BehandlingResultatType.KLAGE_YTELSESVEDTAK_STADFESTET,
                KlageVurdering.STADFESTE_YTELSESVEDTAK, KlageVurdertAv.NK, false);
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

        // Assert
        if (skalSende) {
            verify(dokumentBestillerApplikasjonTjeneste).produserVedtaksbrev(behandlingVedtak);
        } else {
            verify(dokumentBestillerApplikasjonTjeneste, Mockito.never()).produserVedtaksbrev(behandlingVedtak);
        }
    }

    @Test
    public void senderBrevOmUendretUtfallVedRevurdering() {
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(true);
        when(dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(behandling.getId(), DokumentMalType.REVURDERING_DOK))
                .thenReturn(true);

        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

        verify(dokumentBestillerApplikasjonTjeneste).produserVedtaksbrev(behandlingVedtak);
    }

    @Test
    public void senderIkkeBrevOmUendretUtfallHvisIkkeSendtVarselbrevOmRevurdering() {
        when(behandlingVedtak.isBeslutningsvedtak()).thenReturn(true);
        when(dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(behandling.getId(), DokumentMalType.REVURDERING_DOK))
                .thenReturn(false);

        sendVedtaksbrev.sendVedtaksbrev(behandling.getId());

        verify(dokumentBestillerApplikasjonTjeneste, never()).produserVedtaksbrev(behandlingVedtak);
    }
}
