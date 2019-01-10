package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType.IVERKSETT_VEDTAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandling.brev.SendVarselTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingskontrollTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.KonfigVerdi;

@RunWith(CdiRunner.class)
public class HenleggBehandlingTjenesteImplTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private InternalManipulerBehandling manipulerInternBehandling;

    @Mock
    private GrunnlagRepositoryProvider repositoryProviderMock;

    @Mock
    private HistorikkRepository historikkRepositoryMock;

    @Mock
    private BehandlingModellRepository behandlingModellRepository;

    @Mock
    private BehandlingskontrollRepository behandlingskontrollRepository;

    @Mock
    private BehandlingStegType behandlingStegType;

    @Mock
    private BehandlingModell modell;

    @Inject
    private AksjonspunktRepository aksjonspunktRepository;
    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;

    @Inject
    private KodeverkRepository kodeverkRepository;
    @Inject
    @KonfigVerdi(value = "bruker.gruppenavn.saksbehandler")
    private String gruppenavnSaksbehandler;

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    private Behandling behandling;

    @Before
    public void setUp() {
        System.setProperty("systembruker.username", "brukerident");
        
        BehandlingStegType stegType = new BehandlingStegType("dummy") {
                @Override
                public BehandlingStatus getDefinertBehandlingStatus() {
                    return BehandlingStatus.UTREDES;
                }
        };

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forDefaultAktør();
        behandling = scenario.lagMocked();
        repositoryProviderMock = scenario.mockBehandlingRepositoryProvider().getElement1();

        simulerAktivtSteg(behandling, stegType);
        when(repositoryProviderMock.getBehandlingskontrollRepository()).thenReturn(behandlingskontrollRepository);
        when(repositoryProviderMock.getAksjonspunktRepository()).thenReturn(aksjonspunktRepository);
        when(repositoryProviderMock.getKodeverkRepository()).thenReturn(kodeverkRepository);
        when(repositoryProviderMock.getHistorikkRepository()).thenReturn(historikkRepositoryMock);
        when(repositoryProviderMock.getBehandlingRepository().finnBehandlingStegType(IVERKSETT_VEDTAK.getKode())).thenReturn(behandlingStegType);
        BehandlingskontrollTjenesteImpl behandlingskontrollTjenesteImpl = new BehandlingskontrollTjenesteImpl(repositoryProviderMock,
            behandlingModellRepository, null);
        when(behandlingModellRepository.getBehandlingStegKonfigurasjon()).thenReturn(BehandlingStegKonfigurasjon.lagDummy());
        when(behandlingModellRepository.getModell(any(), any())).thenReturn(modell);
        when(modell.erStegAFørStegB(any(), any())).thenReturn(true);

        henleggBehandlingTjeneste = new HenleggBehandlingTjenesteImpl(repositoryProviderMock,
            behandlingskontrollTjenesteImpl,
            prosessTaskRepositoryMock, mock(SendVarselTjeneste.class));
    }

    @Test
    public void skal_henlegge_behandling_med_brev() throws Exception {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET;

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");

        // Assert
        verify(historikkRepositoryMock).lagre(any(Historikkinnslag.class));
        verify(repositoryProviderMock.getBehandlingRepository(), atLeast(2)).lagre(eq(behandling), any(BehandlingLås.class));
    }

    @Test
    public void skal_henlegge_behandling_uten_brev() throws Exception {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");

        // Assert
        verify(historikkRepositoryMock).lagre(any(Historikkinnslag.class));
        verify(repositoryProviderMock.getBehandlingRepository(), atLeast(2)).lagre(eq(behandling), any(BehandlingLås.class));
    }

    @Test
    public void skal_henlegge_behandling_med_aksjonspunkt() throws Exception {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;
        Aksjonspunkt aksjonspunkt = repositoryProviderMock.getAksjonspunktRepository().leggTilAksjonspunkt(behandling,
            AksjonspunktDefinisjon.AVKLAR_FORTSATT_MEDLEMSKAP);
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");

        // Assert
        verify(historikkRepositoryMock).lagre(any(Historikkinnslag.class));
        verify(repositoryProviderMock.getBehandlingRepository(), atLeastOnce()).lagre(eq(behandling), any(BehandlingLås.class));
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.AVBRUTT);
    }

    @Test
    public void skal_henlegge_behandling_ved_dødsfall() throws Exception {
        // Arrange
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_BRUKER_DØD;

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");

        // Assert
        verify(historikkRepositoryMock).lagre(any(Historikkinnslag.class));
        verify(repositoryProviderMock.getBehandlingRepository(), atLeast(2)).lagre(eq(behandling), any(BehandlingLås.class));
    }

    @Test
    public void kan_ikke_henlegge_behandling_som_er_satt_på_vent() throws Exception {
        // Arrange
        AksjonspunktDefinisjon def = AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT;
        Aksjonspunkt aksjonspunkt = repositoryProviderMock.getAksjonspunktRepository().leggTilAksjonspunkt(behandling, def);
        repositoryProviderMock.getAksjonspunktRepository().setFrist(aksjonspunkt, LocalDateTime.now(), null);

        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET;

        // Act
        try {
            henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");
            Assert.fail("Forventet exception");
        } catch (Exception ex) {
            assertThat(ex).hasMessageContaining("FP-154409");
        }
    }

    @Test
    public void kan_henlegge_behandling_der_vedtak_er_foreslått() throws Exception {
        // Arrange
        BehandlingRepository behandlingRepository = repositoryProviderMock.getBehandlingRepository();
        behandlingRepository.lagre(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling), behandlingRepository.taSkriveLås(behandling));
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET;

        // Act
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");
    }

    @Test
    public void kan_ikke_henlegge_behandling_der_vedtak_er_fattet() throws Exception {
        // Arrange
        BehandlingRepository behandlingRepository = repositoryProviderMock.getBehandlingRepository();
        behandlingRepository.lagre(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling), behandlingRepository.taSkriveLås(behandling));
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET;
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, IVERKSETT_VEDTAK);

        // Act
        try {
            henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");
            Assert.fail("Forventet exception");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("FP-143308");
        }
    }

    @Test
    public void kan_ikke_henlegge_behandling_som_allerede_er_henlagt() throws Exception {
        // Arrange
        BehandlingRepository behandlingRepository = repositoryProviderMock.getBehandlingRepository();
        behandlingRepository.lagre(Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.HENLAGT_FEILOPPRETTET).buildFor(behandling), behandlingRepository.taSkriveLås(behandling));
        BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET;

        // Act
        try {
            henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat, "begrunnelse");
            Assert.fail("Forventet exception");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("FP-143308");
        }
    }

    private void simulerAktivtSteg(Behandling behandling, BehandlingStegType stegType) {
        
        BehandlingskontrollTilstand tilstand = new BehandlingskontrollTilstand(behandling.getId(), behandling.getFagsakYtelseType(), behandling.getType());
        tilstand.setStegTilstand(stegType);
        
        when(behandlingskontrollRepository.getBehandlingskontrollTilstand(Mockito.anyLong())).thenReturn(tilstand);
    }
}
