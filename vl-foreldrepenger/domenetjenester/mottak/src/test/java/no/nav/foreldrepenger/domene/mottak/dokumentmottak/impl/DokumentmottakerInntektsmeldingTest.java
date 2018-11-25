package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class DokumentmottakerInntektsmeldingTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private AksjonspunktRepository aksjonspunktRepository;

    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    @Mock
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    @Mock
    private Kompletthetskontroller kompletthetskontroller;
    @Mock
    private Behandlingsoppretter behandlingsoppretter;
    @Mock
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    private DokumentmottakerInntektsmelding dokumentmottaker;
    private DokumentmottakerFelles dokumentmottakerFelles;


    @Before
    public void oppsett() {
        MockitoAnnotations.initMocks(this);

        dokumentmottakerFelles = new DokumentmottakerFelles(prosessTaskRepository, behandlendeEnhetTjeneste, historikkinnslagTjeneste);

        dokumentmottakerFelles = Mockito.spy(dokumentmottakerFelles);

        dokumentmottaker = new DokumentmottakerInntektsmelding(dokumentmottakerFelles, mottatteDokumentTjeneste, behandlingsoppretter,
            kompletthetskontroller, repositoryProvider);
        dokumentmottaker = Mockito.spy(dokumentmottaker);

        OrganisasjonsEnhet enhet = new OrganisasjonsEnhet("0312", "enhetNavn");
        when(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(any(Fagsak.class))).thenReturn(enhet);
        when(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(any(Behandling.class))).thenReturn(enhet);

    }


    @Test
    public void skal_oppdatere_behandling_vurdere_kompletthet_og_spole_til_nytt_startpunkt_dersom_fagsak_har_avsluttet_behandling_har_åpen_behandling_og_kompletthet_passert() {
        // Arrange - opprette avsluttet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        // Arrange - opprette revurdering som har passert kompletthet
        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medFagsakId(behandling.getFagsakId())
                .medBehandlingStegStart(BehandlingStegType.FORESLÅ_VEDTAK)
            .medOriginalBehandling(behandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        Behandling revurderingBehandling = revurderingScenario.lagre(repositoryProvider);

        // Arrange - bygg inntektsmelding
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, revurderingBehandling.getFagsakId(), "", now(), true, "123");

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, revurderingBehandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        // Assert - sjekk flyt
        verify(dokumentmottaker).oppdaterÅpenBehandlingMedDokument(revurderingBehandling, mottattDokument, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        verify(kompletthetskontroller).persisterDokumentOgVurderKompletthet(revurderingBehandling, mottattDokument);
        verify(dokumentmottakerFelles).opprettHistorikkinnslagForVedlegg(behandling.getFagsakId(), mottattDokument.getJournalpostId(), dokumentTypeId);
    }

    @Test
    public void skal_lagre_dokument_og_vurdere_kompletthet_dersom_inntektsmelding_på_åpen_behandling() {
        // Arrange - opprette åpen behandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medBehandlingStegStart(BehandlingStegType.VURDER_KOMPLETTHET);
        Behandling behandling = scenario.lagre(repositoryProvider);
        opprettAksjonspunkt(behandling, AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, LocalDateTime.now());

        // Arrange - bygg inntektsmelding
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "123");

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.UDEFINERT);

        // Assert - sjekk flyt
        verify(kompletthetskontroller).persisterDokumentOgVurderKompletthet(behandling, mottattDokument);
        verify(dokumentmottakerFelles).opprettHistorikkinnslagForVedlegg(behandling.getFagsakId(), mottattDokument.getJournalpostId(), dokumentTypeId);
    }

    @Test
    public void skal_opprette_revurdering_dersom_inntektsmelding_på_avsluttet_behandling() {
        // Arrange - opprette avsluttet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        Behandling revurdering = mock(Behandling.class);

        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "123");
        when(behandlingsoppretter.opprettRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING)).thenReturn(revurdering);

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);

        // Assert
        verify(behandlingsoppretter).opprettRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        verify(mottatteDokumentTjeneste).persisterDokumentinnhold(revurdering, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikkinnslagForVedlegg(behandling.getFagsakId(), mottattDokument.getJournalpostId(), dokumentTypeId);
    }

    @Test
    public void skal_opprette_førstegangsbehandling() {

        Fagsak fagsak = DokumentmottakTestUtil.byggFagsak(new AktørId(123L), new Saksnummer("123"), fagsakRepository);
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, 123L, "", now(), true, "123");
        Behandling førstegangsbehandling = mock(Behandling.class);
        when(førstegangsbehandling.getAktørId()).thenReturn(new AktørId(123L));
        when(behandlingsoppretter.opprettFørstegangsbehandling(fagsak, BehandlingÅrsakType.UDEFINERT)).thenReturn(førstegangsbehandling);

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, fagsak, dokumentTypeId, BehandlingÅrsakType.UDEFINERT);

        // Assert
        verify(behandlingsoppretter).opprettFørstegangsbehandling(fagsak, BehandlingÅrsakType.UDEFINERT);
        verify(dokumentmottakerFelles).opprettHistorikkinnslagForVedlegg(fagsak.getId(), mottattDokument.getJournalpostId(), dokumentTypeId);
    }

    @Test
    public void skal_opprette_køet_revurdering_og_kjøre_kompletthet_dersom_køet_behandling_ikke_finnes() {
        // Arrange - opprette avsluttet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        Behandling revurdering = mock(Behandling.class);

        doReturn(revurdering).when(behandlingsoppretter).opprettKøetBehandling(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        doNothing().when(kompletthetskontroller).vurderKompletthetForKøetBehandling(behandling);

        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "123");

        // Act
        dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);

        // Assert
        verify(behandlingsoppretter).opprettKøetBehandling(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        verify(kompletthetskontroller).persisterKøetDokumentOgVurderKompletthet(revurdering, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikk(revurdering, mottattDokument.getJournalpostId());
    }

    @Test
    public void skal_opprette_køet_behandling_og_kjøre_kompletthet_dersom_køet_behandling_ikke_finnes() {
        // Arrange - opprette fagsak uten behandling
        Fagsak fagsak = DokumentmottakTestUtil.byggFagsak(new AktørId("1"), new Saksnummer("123"), fagsakRepository);

        // Arrange - sett opp opprettelse av køet behandling
        Behandling behandling = mock(Behandling.class);
        doReturn(behandling).when(behandlingsoppretter).opprettKøetBehandling(fagsak, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);

        // Arrange - bygg inntektsmelding
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsak.getId(), "", now(), true, "123");

        // Act
        dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, fagsak, dokumentTypeId, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);

        // Assert - sjekk flyt
        verify(behandlingsoppretter).opprettKøetBehandling(fagsak, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        verify(kompletthetskontroller).persisterKøetDokumentOgVurderKompletthet(behandling, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Test
    public void skal_oppdatere_køet_behandling_og_kjøre_kompletthet_dersom_køet_behandling_finnes() {
        // Arrange - opprette køet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        simulerKøetBehandling(behandling);

        // Act - send inntektsmelding
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "123");
        dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.UDEFINERT);

        // Assert - verifiser flyt
        verify(kompletthetskontroller).persisterKøetDokumentOgVurderKompletthet(behandling, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    private void simulerKøetBehandling(Behandling behandling) {
        BehandlingÅrsakType berørtType = kodeverkRepository.finn(BehandlingÅrsakType.class, BehandlingÅrsakType.KØET_BEHANDLING);
        new BehandlingÅrsak.Builder(Arrays.asList(berørtType)).buildFor(behandling);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING);
    }

    private Aksjonspunkt opprettAksjonspunkt(Behandling behandling,
                                             AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                             LocalDateTime frist) {

        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        aksjonspunktRepository.setFrist(aksjonspunkt, frist, Venteårsak.UDEFINERT);
        return aksjonspunkt;
    }
}
