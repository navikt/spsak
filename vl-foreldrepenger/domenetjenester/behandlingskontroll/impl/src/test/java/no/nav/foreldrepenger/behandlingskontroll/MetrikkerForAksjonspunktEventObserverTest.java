package no.nav.foreldrepenger.behandlingskontroll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import no.nav.foreldrepenger.behandlingskontroll.observer.MetrikkerForAksjonspunktEventObserver;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;

public class MetrikkerForAksjonspunktEventObserverTest {

    private MetrikkerForAksjonspunktEventObserver observer; // objectet vi tester

    private MetricRegistry mockMetricRegistry;
    private Timer mockMetricTimer;
    private BehandlingRepository mockBehandlingRepository;
    private FagsakRepository mockFagsakRepository;
    private KodeverkRepository mockKodeverkRepository;
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingskontrollKontekst mockKontekst;
    private Behandling mockBehandling;
    private Aksjonspunkt mockAksjonspunktNyeste;
    private Fagsak mockFagsak;

    private Set<Aksjonspunkt> aksjonspunktSet;

    private static final String GRAFANA_KEY_ENGANGSSTØNAD_FØDSEL = "fpsak.ab0050.behandling.tid.ledetid.forste.manuell";
    private static final String GRAFANA_KEY_ENGANGSSTØNAD_ADOPSJON = "fpsak.ab0027.behandling.tid.ledetid.forste.manuell";
    private FamilieHendelseRepository familieHendelseRepository;

    @Before
    public void setup() {

        mockMetricRegistry = mock(MetricRegistry.class);
        mockMetricTimer = mock(Timer.class);
        mockBehandlingRepository = mock(BehandlingRepository.class);
        mockFagsakRepository = mock(FagsakRepository.class);
        familieHendelseRepository = mock(FamilieHendelseRepository.class);
        mockKodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        repositoryProvider = mock(BehandlingRepositoryProvider.class);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(mockBehandlingRepository);
        when(repositoryProvider.getKodeverkRepository()).thenReturn(mockKodeverkRepository);
        when(repositoryProvider.getFagsakRepository()).thenReturn(mockFagsakRepository);
        when(repositoryProvider.getFamilieGrunnlagRepository()).thenReturn(familieHendelseRepository);
        mockKontekst = mock(BehandlingskontrollKontekst.class);
        mockBehandling = mock(Behandling.class);

        mockAksjonspunktNyeste = mock(Aksjonspunkt.class);
        mockFagsak = mock(Fagsak.class);

        aksjonspunktSet = new HashSet<>();
        aksjonspunktSet.add(mockAksjonspunktNyeste);

        when(mockMetricRegistry.timer(any(String.class))).thenReturn(mockMetricTimer);
        LocalDateTime opprettetDato = LocalDateTime.now().minusDays(2);
        when(mockBehandling.getOpprettetDato()).thenReturn(opprettetDato);
        when(mockBehandlingRepository.hentBehandling(any(Long.class))).thenReturn(mockBehandling);
        when(mockBehandling.getAksjonspunkter()).thenReturn(aksjonspunktSet);
        when(mockAksjonspunktNyeste.getId()).thenReturn(123L);
        when(mockAksjonspunktNyeste.getStatus()).thenReturn(AksjonspunktStatus.UTFØRT);
        when(mockFagsakRepository.finnEksaktFagsak(any(Long.class))).thenReturn(mockFagsak);

        observer = new MetrikkerForAksjonspunktEventObserver(mockMetricRegistry, repositoryProvider);
    }

    @Test
    public void skalMåleTidForFørsteAksjonspunktUtførtFødsel() {

        AksjonspunktUtførtEvent event = new AksjonspunktUtførtEvent(mockKontekst, Arrays.asList(mockAksjonspunktNyeste), BehandlingStegType.INNHENT_REGISTEROPP);

        mockFamilieHendelse(FamilieHendelseType.FØDSEL);
        when(mockFagsak.getYtelseType()).thenReturn(FagsakYtelseType.ENGANGSTØNAD);

        // ikke legg til flere i aksjonspunktSet

        observer.måleTidFraBehandlingÅpnetTilFørsteAksjonspunktUtført(event);

        verify(mockBehandlingRepository).hentBehandling(any(Long.class));
        verify(mockFagsakRepository).finnEksaktFagsak(any(Long.class));
        verify(mockMetricRegistry).timer(GRAFANA_KEY_ENGANGSSTØNAD_FØDSEL);
        verify(mockMetricTimer).update(any(Long.class), any(TimeUnit.class));
    }

    @Test
    public void skalMåleTidForFørsteAksjonspunktUtførtAdopsjon() {

        AksjonspunktUtførtEvent event = new AksjonspunktUtførtEvent(mockKontekst, Arrays.asList(mockAksjonspunktNyeste), BehandlingStegType.INNHENT_REGISTEROPP);

        mockFamilieHendelse(FamilieHendelseType.ADOPSJON);
        when(mockFagsak.getYtelseType()).thenReturn(FagsakYtelseType.ENGANGSTØNAD);

        Aksjonspunkt mockAksjonspunktÅpent = mock(Aksjonspunkt.class);
        when(mockAksjonspunktÅpent.getId()).thenReturn(777L);
        when(mockAksjonspunktÅpent.getStatus()).thenReturn(AksjonspunktStatus.OPPRETTET);
        aksjonspunktSet.add(mockAksjonspunktÅpent);

        Aksjonspunkt mockAksjonspunktAuto = mock(Aksjonspunkt.class);
        when(mockAksjonspunktAuto.getId()).thenReturn(1024L);
        when(mockAksjonspunktAuto.getStatus()).thenReturn(AksjonspunktStatus.UTFØRT);
        when(mockAksjonspunktAuto.erAutopunkt()).thenReturn(true);
        aksjonspunktSet.add(mockAksjonspunktAuto);

        observer.måleTidFraBehandlingÅpnetTilFørsteAksjonspunktUtført(event);

        verify(mockBehandlingRepository).hentBehandling(any(Long.class));
        verify(mockFagsakRepository).finnEksaktFagsak(any(Long.class));
        verify(mockMetricRegistry).timer(GRAFANA_KEY_ENGANGSSTØNAD_ADOPSJON);
        verify(mockMetricTimer).update(any(Long.class), any(TimeUnit.class));
    }

    @Test
    public void skalIkkeMåleTidForAndreAksjonspunktUtført() {

        AksjonspunktUtførtEvent event = new AksjonspunktUtførtEvent(mockKontekst, Arrays.asList(mockAksjonspunktNyeste), BehandlingStegType.INNHENT_REGISTEROPP);

        mockFamilieHendelse(FamilieHendelseType.FØDSEL);
        when(mockFagsak.getYtelseType()).thenReturn(FagsakYtelseType.ENGANGSTØNAD);

        // Et annet ikke-åpent aksjonspunkt - skal hindre måling
        Aksjonspunkt mockAksjonspunktLukket = mock(Aksjonspunkt.class);
        when(mockAksjonspunktLukket.getId()).thenReturn(983L);
        when(mockAksjonspunktLukket.getStatus()).thenReturn(AksjonspunktStatus.AVBRUTT);
        when(mockAksjonspunktLukket.erManuell()).thenReturn(true);
        when(mockAksjonspunktLukket.erAutopunkt()).thenReturn(false);
        aksjonspunktSet.add(mockAksjonspunktLukket);

        observer.måleTidFraBehandlingÅpnetTilFørsteAksjonspunktUtført(event);

        verify(mockBehandlingRepository).hentBehandling(any(Long.class));
        verify(mockFagsakRepository, never()).finnEksaktFagsak(any(Long.class));
        verify(mockMetricRegistry, never()).timer(GRAFANA_KEY_ENGANGSSTØNAD_FØDSEL);
        verify(mockMetricTimer, never()).update(any(Long.class), any(TimeUnit.class));
    }

    private void mockFamilieHendelse(FamilieHendelseType type) {
        final Behandling mock1 = mock(Behandling.class);
        when(mockBehandlingRepository.hentSisteBehandlingForFagsakId(any())).thenReturn(Optional.of(mock1));
        FamilieHendelseGrunnlag mock = mock(FamilieHendelseGrunnlag.class);
        FamilieHendelse mmock = mock(FamilieHendelse.class);
        when(mmock.getType()).thenReturn(type);
        when(mmock.getGjelderFødsel()).thenReturn((type.equals(FamilieHendelseType.FØDSEL) || type.equals(FamilieHendelseType.TERMIN)));
        when(mmock.getGjelderAdopsjon()).thenReturn((type.equals(FamilieHendelseType.ADOPSJON) || type.equals(FamilieHendelseType.OMSORG)));
        when(mock.getSøknadVersjon()).thenReturn(mmock);
        when(mock.getGjeldendeVersjon()).thenReturn(mmock);
        when(familieHendelseRepository.hentAggregat(any())).thenReturn(mock);
        when(familieHendelseRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(mock));
    }
}
