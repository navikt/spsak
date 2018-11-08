package no.nav.foreldrepenger.behandling.steg.klage.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioKlageEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;

public class KlageNfpStegImplTest {

    private HistorikkRepository historikkRepositoryMock;
    private DatavarehusTjeneste datavarehusTjenesteMock;
    private KlageNfpStegImpl steg;
    private BehandlingskontrollKontekst kontekst;
    private KodeverkRepository kodeverkRepositoryMock;
    private final BehandlingRepositoryProvider repositoryProviderMock = mock(BehandlingRepositoryProvider.class);

    @Before
    public void setUp() {
        kontekst = mock(BehandlingskontrollKontekst.class);
        historikkRepositoryMock = mock(HistorikkRepository.class);
        datavarehusTjenesteMock = mock(DatavarehusTjeneste.class);
        BehandlingRepository behandlingRepositoryMock = mock(BehandlingRepository.class);
        kodeverkRepositoryMock = mock(KodeverkRepository.class);
        when(repositoryProviderMock.getBehandlingRepository()).thenReturn(behandlingRepositoryMock);
        when(repositoryProviderMock.getKodeverkRepository()).thenReturn(kodeverkRepositoryMock);

        steg = new KlageNfpStegImpl(repositoryProviderMock, datavarehusTjenesteMock, historikkRepositoryMock);
    }

    @Test
    public void skalOppretteAksjonspunktManuellVurderingAvKlageNfpNårStegKjøres() {
        // Act
        BehandleStegResultat behandlingStegResultat = steg.utførSteg(kontekst);

        // Assert
        assertThat(behandlingStegResultat).isNotNull();
        assertThat(behandlingStegResultat.getTransisjon()).isEqualTo(FellesTransisjoner.UTFØRT);
        assertThat(behandlingStegResultat.getAksjonspunktListe().size()).isEqualTo(1);

        AksjonspunktDefinisjon aksjonspunktDefinisjon = behandlingStegResultat.getAksjonspunktListe().get(0);
        assertThat(aksjonspunktDefinisjon).isEqualTo(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NFP);
    }

    @Test
    public void skalOverhoppBakoverRyddeKlageVurderingRestultatOgLageHistorikkInnslag() {
        // Arrange

        ScenarioKlageEngangsstønad scenario = ScenarioKlageEngangsstønad.forMedholdNK(ScenarioMorSøkerEngangsstønad.forFødsel());
        Behandling klageBehandling = scenario.lagMocked();
        BehandlingRepository mockedBehandlingRepository = scenario.mockBehandlingRepository();
        BehandlingRepositoryProvider repositoryProviderMock = scenario.mockBehandlingRepositoryProvider();
        steg = new KlageNfpStegImpl(repositoryProviderMock, datavarehusTjenesteMock, historikkRepositoryMock);

        // Act
        steg.vedTransisjon(kontekst, klageBehandling, null, BehandlingSteg.TransisjonType.HOPP_OVER_BAKOVER, null, null, BehandlingSteg.TransisjonType.FØR_INNGANG);

        // Assert
        verify(mockedBehandlingRepository).slettKlageVurderingResultat(any(), any(), eq(KlageVurdertAv.NFP));

        ArgumentCaptor<Historikkinnslag> historikkCaptor = ArgumentCaptor.forClass(Historikkinnslag.class);
        verify(historikkRepositoryMock, times(1)).lagre(historikkCaptor.capture());
        Historikkinnslag historikkinnslag = historikkCaptor.getValue();
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.BYTT_ENHET);
        assertThat(historikkinnslag.getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);
    }
}
