package no.nav.foreldrepenger.behandlingskontroll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingskontroll.observer.HistorikkInnslagForAksjonspunktEventObserver;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class BehandlingskontrollAksjonspunktTypeAutopunktEventObserverTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();
    private HistorikkInnslagForAksjonspunktEventObserver observer; // objectet vi tester

    private BehandlingskontrollKontekst behandlingskontrollKontekst;
    private AksjonspunktDefinisjon autopunktDefinisjon;
    private AksjonspunktDefinisjon manuellpunktDefinisjon;
    private AksjonspunktDefinisjon autopunktDefSomSkalUtenHistorikk;
    private Aksjonspunkt autopunkt;
    private Aksjonspunkt manuellpunkt;
    private Aksjonspunkt autopunktSomSkalUtenHistorikk;
    private HistorikkRepository historikkRepository;
    private Long behandlingId = 1L;
    private String PERIODE = "P2W";
    private LocalDate localDate = LocalDate.now().plus(Period.parse(PERIODE));



    @Before
    public void setup() {
        autopunktDefinisjon = AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT;
        manuellpunktDefinisjon = AksjonspunktDefinisjon.MANUELL_VURDERING_AV_MEDLEMSKAP;

        manuellpunkt = Mockito.mock(Aksjonspunkt.class);
        when(manuellpunkt.getAksjonspunktDefinisjon()).thenReturn(manuellpunktDefinisjon);

        autopunkt = Mockito.mock(Aksjonspunkt.class);
        when(autopunkt.getAksjonspunktDefinisjon()).thenReturn(autopunktDefinisjon);
        when(autopunkt.getFristTid()).thenReturn(LocalDateTime.of(localDate, LocalDateTime.now().toLocalTime()));

        autopunktDefSomSkalUtenHistorikk = Mockito.mock(AksjonspunktDefinisjon.class);
        when(autopunktDefSomSkalUtenHistorikk.getLagUtenHistorikk()).thenReturn(true);

        autopunktSomSkalUtenHistorikk = Mockito.mock(Aksjonspunkt.class);
        when(autopunktSomSkalUtenHistorikk.getAksjonspunktDefinisjon()).thenReturn(autopunktDefSomSkalUtenHistorikk);
        when(autopunktSomSkalUtenHistorikk.getFristTid()).thenReturn(LocalDateTime.of(localDate, LocalDateTime.now().toLocalTime()));

        behandlingskontrollKontekst = mock(BehandlingskontrollKontekst.class);
        when(behandlingskontrollKontekst.getBehandlingId()).thenReturn(behandlingId);

        historikkRepository = mock(HistorikkRepository.class);
        observer = new HistorikkInnslagForAksjonspunktEventObserver(historikkRepository, "srvengangsstonad");
    }

    @Test
    public void skalMåleTidForFørsteAksjonspunktUtførtFødsel() {

        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(autopunkt), null);

        observer.oppretteHistorikkForBehandlingPåVent(event);

        verify(historikkRepository).lagre(any(Historikkinnslag.class));
    }

    @Test
    public void skalIkkeOppretteHistorikkForManuellPunkt() {

        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(manuellpunkt), null);

        observer.oppretteHistorikkForBehandlingPåVent(event);

        verify(historikkRepository, never()).lagre(any());
    }

    @Test
    public void skalIkkeOppretteHistorikkForPunkterMarkertMedLagUtenHistorikk() {

        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(autopunktSomSkalUtenHistorikk), null);

        observer.oppretteHistorikkForBehandlingPåVent(event);

        verify(historikkRepository, never()).lagre(any());
    }

    @Test
    public void skalOppretteEnHistorikkForAutoPunktOgSjekkPåResultat() {

        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(manuellpunkt, autopunkt), null);

        ArgumentCaptor<Historikkinnslag> captor = ArgumentCaptor.forClass(Historikkinnslag.class);

        observer.oppretteHistorikkForBehandlingPåVent(event);

        verify(historikkRepository).lagre(captor.capture());
        Historikkinnslag historikkinnslag = captor.getValue();
        HistorikkinnslagDel historikkinnslagDel = historikkinnslag.getHistorikkinnslagDeler().get(0);

        assertThat(historikkinnslag.getBehandlingId()).isEqualTo(behandlingId);
        assertThat(historikkinnslagDel.getHendelse()).hasValueSatisfying(hendelse -> {
            assertThat(hendelse.getNavn()).as("navn").isEqualTo(HistorikkinnslagType.BEH_VENT.getKode());
            assertThat(hendelse.getTilVerdi()).as("tilVerdi").isEqualTo(localDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        });
        assertThat(historikkinnslag.getType()).isEqualTo(HistorikkinnslagType.BEH_VENT);
    }

    @Test
    public void skalOppretteToHistorikkForAutoPunkt() {

        AksjonspunkterFunnetEvent event = new AksjonspunkterFunnetEvent(behandlingskontrollKontekst, Arrays.asList(autopunkt, autopunkt), null);

        observer.oppretteHistorikkForBehandlingPåVent(event);

        verify(historikkRepository, times(2)).lagre(any());
    }

}
