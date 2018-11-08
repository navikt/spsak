package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.time.LocalDate.now;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_KOMPLETT_OPPDATERING;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetModell;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetsjekkerProvider;
import no.nav.foreldrepenger.domene.registerinnhenting.Endringskontroller;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KompletthetskontrollerTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    // Må initialiseres her for at kodeverkTabellRepository skal lastes riktig, ingen mock-støtte for dette :(
    private KodeverkTabellRepository kodeverkTabellRepository = kodeverkRepository.getKodeverkTabellRepository();

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Mock
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Mock
    private KompletthetsjekkerProvider kompletthetsjekkerProvider;

    @Mock
    private DokumentmottakerFelles dokumentmottakerFelles;

    @Mock
    private EndringsresultatSjekker endringsresultatSjekker;

    @Mock
    private RegisterdataEndringshåndterer registerdataEndringshåndterer;

    @Mock
    private Endringskontroller endringskontroller;

    @Mock
    private Kompletthetsjekker kompletthetsjekker;

    @Mock
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;

    private Kompletthetskontroller kompletthetskontroller;
    private Behandling behandling;
    private MottattDokument mottattDokument;

    @Before
    public void oppsett() {
        MockitoAnnotations.initMocks(this);

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagMocked();

        // Simuler at provider alltid gir kompletthetssjekker
        when(kompletthetsjekkerProvider.finnKompletthetsjekkerFor(behandling)).thenReturn(kompletthetsjekker);

        KompletthetModell modell = new KompletthetModell(behandlingskontrollTjeneste, kompletthetsjekkerProvider, kodeverkTabellRepository);
        kompletthetskontroller = new Kompletthetskontroller(behandlingskontrollTjeneste,
            dokumentmottakerFelles,
            endringsresultatSjekker, registerdataEndringshåndterer,
            endringskontroller, mottatteDokumentTjeneste, modell);

        mottattDokument = DokumentmottakTestUtil.byggMottattDokument(DokumentTypeId.INNTEKTSMELDING, behandling.getFagsakId(), "", now(), true, null);

    }

    @Test
    public void skal_sette_behandling_på_vent_dersom_kompletthet_ikke_er_oppfylt() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, BehandlingStegType.VURDER_KOMPLETTHET);
        Behandling behandling = scenario.lagre(repositoryProvider); // Skulle gjerne mocket, men da funker ikke AP_DEF
        LocalDateTime ventefrist = LocalDateTime.now().plusDays(1);

        when(kompletthetsjekkerProvider.finnKompletthetsjekkerFor(behandling)).thenReturn(kompletthetsjekker);
        when(kompletthetsjekker.vurderForsendelseKomplett(behandling)).thenReturn(KompletthetResultat.ikkeOppfylt(ventefrist, Venteårsak.AVV_FODSEL));

        kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);

        verify(endringskontroller).settPåVent(behandling, AUTO_VENT_KOMPLETT_OPPDATERING, ventefrist, Venteårsak.AVV_FODSEL);
    }

    @Test
    public void skal_gjenoppta_behandling_dersom_behandling_er_komplett_og_kompletthet_ikke_passert() {
        // Arrange
        when(kompletthetsjekker.vurderForsendelseKomplett(behandling)).thenReturn(KompletthetResultat.oppfylt());
        when(behandlingskontrollTjeneste.erStegPassert(behandling, BehandlingStegType.VURDER_KOMPLETTHET)).thenReturn(false);

        kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);

        verify(endringskontroller).gjenoppta(behandling);
    }

    @Test
    public void skal_spole_til_startpunkt_dersom_komplett_og_vurder_kompletthet_er_passert() {
        // Arrange
        when(kompletthetsjekker.vurderForsendelseKomplett(behandling)).thenReturn(KompletthetResultat.oppfylt());
        when(behandlingskontrollTjeneste.erStegPassert(behandling, BehandlingStegType.VURDER_KOMPLETTHET)).thenReturn(true);

        EndringsresultatSnapshot endringsresultatSnapshot = EndringsresultatSnapshot.opprett();
        when(endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling)).thenReturn(endringsresultatSnapshot);

        EndringsresultatDiff endringsresultat = EndringsresultatDiff.opprett();
        endringsresultat.leggTilIdDiff(EndringsresultatDiff.medDiff(PersonInformasjon.class, endringsresultatSnapshot.getGrunnlagId(), 1L));
        when(registerdataEndringshåndterer.oppdaterRegisteropplysninger(behandling, endringsresultatSnapshot)).thenReturn(endringsresultat);

        // Act - send inntektsmelding
        kompletthetskontroller.persisterDokumentOgVurderKompletthet(behandling, mottattDokument);

        // Assert
        verify(endringskontroller).taAvVent(behandling, AUTO_VENT_KOMPLETT_OPPDATERING);
        // Selve spolingen delegeres til RegisterdataEndringshåndterer
        verify(registerdataEndringshåndterer).oppdaterRegisteropplysninger(behandling, endringsresultatSnapshot);
    }

    @Test
    public void skal_opprette_historikkinnslag_for_tidlig_mottatt_søknad() {
        // Arrange
        LocalDateTime frist = LocalDateTime.now();
        when(kompletthetsjekker.vurderSøknadMottatt(behandling)).thenReturn(KompletthetResultat.oppfylt());
        when(kompletthetsjekker.vurderSøknadMottattForTidlig(behandling)).thenReturn(KompletthetResultat.ikkeOppfylt(frist, Venteårsak.FOR_TIDLIG_SOKNAD));

        // Act
        kompletthetskontroller.persisterKøetDokumentOgVurderKompletthet(behandling, mottattDokument, Optional.empty());

        // Assert
        verify(mottatteDokumentTjeneste).persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikkinnslagForVenteFristRelaterteInnslag(behandling, HistorikkinnslagType.BEH_VENT, frist, Venteårsak.FOR_TIDLIG_SOKNAD);
    }

    @Test
    public void skal_opprette_historikkinnslag_ikke_komplett() {
        // Arrange
        LocalDateTime frist = LocalDateTime.now();
        when(kompletthetsjekker.vurderSøknadMottatt(behandling)).thenReturn(KompletthetResultat.oppfylt());
        when(kompletthetsjekker.vurderSøknadMottattForTidlig(behandling)).thenReturn(KompletthetResultat.oppfylt());
        when(kompletthetsjekker.vurderForsendelseKomplett(behandling)).thenReturn(KompletthetResultat.ikkeOppfylt(frist, Venteårsak.AVV_DOK));

        // Act
        kompletthetskontroller.persisterKøetDokumentOgVurderKompletthet(behandling, mottattDokument, Optional.empty());

        // Assert
        verify(mottatteDokumentTjeneste).persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikkinnslagForVenteFristRelaterteInnslag(behandling, HistorikkinnslagType.BEH_VENT, frist, Venteårsak.AVV_DOK);
    }
}
