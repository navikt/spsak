package no.nav.foreldrepenger.dokumentbestiller.observers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.dokumentbestiller.autopunkt.SendBrevForAutopunkt;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class SendBrevForAutopunktEventObserverTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Mock
    private Aksjonspunkt autopunktIngenSøknad;
    @Mock
    private Aksjonspunkt autopunktVentFødsel;
    @Mock
    private Aksjonspunkt autopunktTidligSøknad;
    @Mock
    private Aksjonspunkt manuellpunkt;
    @Mock
    private BehandlingRepository behandlingRepository;
    @Mock
    private SendBrevForAutopunkt sendBrevForAutopunkt;

    private SendBrevForAutopunktEventObserver observer; // objektet vi tester

    private BehandlingskontrollKontekst behandlingskontrollKontekst;
    private AksjonspunktDefinisjon autopunktDefinisjonIngenSøknad;
    private AksjonspunktDefinisjon manuellpunktDefinisjon;
    private AksjonspunktDefinisjon autopunktDefinisjonTidligSøknad;
    private AksjonspunktDefinisjon autopunktDefinisjonVentFødsel;

    private Long behandlingId = 1L;
    private String PERIODE = "P2W";
    private LocalDate localDate = LocalDate.now().plus(Period.parse(PERIODE));

    @Before
    public void setUp() {
        initMocks(this);

        autopunktDefinisjonIngenSøknad = AksjonspunktDefinisjon.VENT_PÅ_SØKNAD;
        autopunktDefinisjonTidligSøknad = AksjonspunktDefinisjon.VENT_PGA_FOR_TIDLIG_SØKNAD;
        autopunktDefinisjonVentFødsel = AksjonspunktDefinisjon.VENT_PÅ_FØDSEL;

        manuellpunktDefinisjon = AksjonspunktDefinisjon.MANUELL_VURDERING_AV_MEDLEMSKAP;

        when(manuellpunkt.getAksjonspunktDefinisjon()).thenReturn(manuellpunktDefinisjon);

        when(autopunktIngenSøknad.getAksjonspunktDefinisjon()).thenReturn(autopunktDefinisjonIngenSøknad);
        when(autopunktVentFødsel.getAksjonspunktDefinisjon()).thenReturn(autopunktDefinisjonVentFødsel);
        when(autopunktTidligSøknad.getAksjonspunktDefinisjon()).thenReturn(autopunktDefinisjonTidligSøknad);


        behandlingskontrollKontekst = mock(BehandlingskontrollKontekst.class);
        when(behandlingskontrollKontekst.getBehandlingId()).thenReturn(behandlingId);

        observer = new SendBrevForAutopunktEventObserver(behandlingRepository, sendBrevForAutopunkt);
    }

    @Test
    public void skalIkkeSendeBrevForAndreAksjonspunkter() {

        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(manuellpunkt), null);

        observer.sendBrevForAutopunkt(event);

        verify(sendBrevForAutopunkt, times(0)).sendBrevForSøknadIkkeMottatt(any());
        verify(sendBrevForAutopunkt, times(0)).sendBrevForVenterPåFødsel(any(), any());
        verify(sendBrevForAutopunkt, times(0)).sendBrevForTidligSøknad(any(), any());
    }


    @Test
    public void skalSendeBrevForSøknadIkkeMottatt() {
        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(autopunktIngenSøknad), null);
        observer.sendBrevForAutopunkt(event);
        verify(sendBrevForAutopunkt, times(1)).sendBrevForSøknadIkkeMottatt(any());
    }

    @Test
    public void skalSendeBrevForTidligSøknad() {
        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(autopunktTidligSøknad), null);
        observer.sendBrevForAutopunkt(event);
        verify(sendBrevForAutopunkt, times(1)).sendBrevForTidligSøknad(any(), any());
    }

    @Test
    public void skalSendeBrevForVenterFødsel() {
        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(autopunktVentFødsel), null);
        observer.sendBrevForAutopunkt(event);
        verify(sendBrevForAutopunkt, times(1)).sendBrevForVenterPåFødsel(any(), any());
    }

}
