package no.nav.foreldrepenger.dokumentbestiller.autopunkt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteImpl;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class SendBrevForAutopunktImplTest {

    @Spy
    DokumentBestillerApplikasjonTjenesteImpl dokumentBestillerApplikasjonTjeneste;

    Aksjonspunkt aksjonspunkt;

    private SendBrevForAutopunkt sendBrevForAutopunkt;

    private Behandling behandling;
    private ScenarioMorSøkerForeldrepenger scenario;
    private FakeUnleash unleash;
    private LocalDateTime nå = LocalDateTime.now();

    @Before
    public void setUp() {
        initMocks(this);
        scenario = ScenarioMorSøkerForeldrepenger.forFødsel().medDefaultBekreftetTerminbekreftelse();
        BehandlingRepositoryProvider repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.VENT_PÅ_FØDSEL, BehandlingStegType.KONTROLLER_FAKTA);
        behandling = scenario.lagMocked();
        aksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VENT_PÅ_FØDSEL).get();

        repositoryProvider.getAksjonspunktRepository().setFrist(aksjonspunkt, nå, Venteårsak.AVV_FODSEL);

        unleash = new FakeUnleash();
        sendBrevForAutopunkt = new SendBrevForAutopunktImpl(dokumentBestillerApplikasjonTjeneste,
            repositoryProvider,
            unleash);

        doReturn(false).when(dokumentBestillerApplikasjonTjeneste).erDokumentProdusert(Mockito.eq(behandling.getId()), Mockito.anyString());
        doReturn(123l).when(dokumentBestillerApplikasjonTjeneste).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
    }

    @Test
    public void sendBrevForSøknadIkkeMottattFørsteGang() {
        sendBrevForAutopunkt.sendBrevForSøknadIkkeMottatt(behandling);
        Mockito.verify(dokumentBestillerApplikasjonTjeneste, times(1)).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
    }

    @Test
    public void skalBareSendeBrevForSøknadIkkeMottattFørsteGang() {
        doReturn(true).when(dokumentBestillerApplikasjonTjeneste).erDokumentProdusert(behandling.getId(), DokumentMalType.INNTEKTSMELDING_FOR_TIDLIG_DOK);
        sendBrevForAutopunkt.sendBrevForSøknadIkkeMottatt(behandling);
        Mockito.verify(dokumentBestillerApplikasjonTjeneste, times(0)).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
    }

    @Test
    public void sendBrevForTidligSøknadUtenToggle() {
        sendBrevForAutopunkt.sendBrevForTidligSøknad(behandling, aksjonspunkt);
        Mockito.verify(dokumentBestillerApplikasjonTjeneste, times(0)).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
    }

    @Test
    public void sendBrevForTidligSøknadFørsteGangMedToggle() {
        unleash.enableAll();
        sendBrevForAutopunkt.sendBrevForTidligSøknad(behandling, aksjonspunkt);
        Mockito.verify(dokumentBestillerApplikasjonTjeneste, times(1)).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
        assertThat(behandling.getBehandlingstidFrist()).isEqualTo(LocalDate.from(nå.plusWeeks(behandling.getType().getBehandlingstidFristUker())));
    }

    @Test
    public void sendBrevForTidligSøknadBareEnGang() {
        unleash.enableAll();
        doReturn(true).when(dokumentBestillerApplikasjonTjeneste).erDokumentProdusert(behandling.getId(), DokumentMalType.FORLENGET_TIDLIG_SOK);
        sendBrevForAutopunkt.sendBrevForTidligSøknad(behandling, aksjonspunkt);
        Mockito.verify(dokumentBestillerApplikasjonTjeneste, times(0)).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
    }

    @Test
    public void sendBrevForVenterPåFødsel() {
        Aksjonspunkt spyAp = Mockito.spy(aksjonspunkt);
        AksjonspunktDefinisjon mockApDef = Mockito.mock(AksjonspunktDefinisjon.class);
        doReturn("P1M").when(mockApDef).getFristPeriode();
        doReturn(mockApDef).when(spyAp).getAksjonspunktDefinisjon();
        sendBrevForAutopunkt.sendBrevForVenterPåFødsel(behandling, spyAp);
        Mockito.verify(dokumentBestillerApplikasjonTjeneste, times(1)).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
        assertThat(behandling.getBehandlingstidFrist()).isAfter(LocalDate.from(nå));
    }

    @Test
    public void sendBrevForVenterFødselBareEnGang() {
        doReturn(true).when(dokumentBestillerApplikasjonTjeneste).erDokumentProdusert(behandling.getId(), DokumentMalType.FORLENGET_MEDL_DOK);
        Aksjonspunkt spyAp = Mockito.spy(aksjonspunkt);
        AksjonspunktDefinisjon mockApDef = Mockito.mock(AksjonspunktDefinisjon.class);
        doReturn("P1M").when(mockApDef).getFristPeriode();
        doReturn(mockApDef).when(spyAp).getAksjonspunktDefinisjon();
        sendBrevForAutopunkt.sendBrevForVenterPåFødsel(behandling, spyAp);
        Mockito.verify(dokumentBestillerApplikasjonTjeneste, times(0)).bestillDokument(Mockito.any(), Mockito.eq(HistorikkAktør.VEDTAKSLØSNINGEN));
    }

}
