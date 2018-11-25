package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
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
public class DokumentmottakerEndringssøknadTest {

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
    private Behandlingsoppretter behandlingsoppretter;
    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private BehandlendeEnhetTjeneste enhetsTjeneste;

    @Mock
    private KøKontroller køKontroller;
    @Mock
    private Kompletthetskontroller kompletthetskontroller;
    @Mock
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    @Mock
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    private DokumentmottakerEndringssøknad dokumentmottaker;
    private DokumentmottakerFelles dokumentmottakerFelles;

    @Before
    public void oppsett() {
        MockitoAnnotations.initMocks(this);

        OrganisasjonsEnhet enhet = new OrganisasjonsEnhet("0312", "enhetNavn");
        when(enhetsTjeneste.finnBehandlendeEnhetFraSøker(any(Fagsak.class))).thenReturn(enhet);
        when(enhetsTjeneste.finnBehandlendeEnhetFraSøker(any(Behandling.class))).thenReturn(enhet);

        dokumentmottakerFelles = new DokumentmottakerFelles(prosessTaskRepository,
            enhetsTjeneste, historikkinnslagTjeneste);
        dokumentmottakerFelles = Mockito.spy(dokumentmottakerFelles);

        dokumentmottaker = new DokumentmottakerEndringssøknad(repositoryProvider, dokumentmottakerFelles,
            mottatteDokumentTjeneste, behandlingsoppretter, kompletthetskontroller, køKontroller);
        dokumentmottaker = Mockito.spy(dokumentmottaker);
    }

    @Test
    public void skal_opprette_task_for_manuell_vurdering_av_endringssøknad_dersom_ingen_behandling_finnes_fra_før() {
        //Arrange
        Fagsak fagsak = nyMorFødselFagsak();
        Long fagsakId = fagsak.getId();
        DokumentTypeId dokumentTypeEndringssøknad = DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;

        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeEndringssøknad, fagsakId, "", now(), true, null);

        //Act
        dokumentmottaker.mottaDokument(mottattDokument, fagsak, dokumentTypeEndringssøknad, BehandlingÅrsakType.UDEFINERT);

        //Assert
        verify(dokumentmottakerFelles).opprettTaskForÅVurdereDokument(fagsak, null, mottattDokument);
    }

    @Test
    public void skal_opprette_revurdering_for_endringssøknad_dersom_siste_behandling_er_avsluttet() {
        //Arrange
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forDefaultAktør(false)
            .lagre(repositoryProvider);
        BehandlingVedtak vedtak = DokumentmottakTestUtil.oppdaterVedtaksresultat(behandling, VedtakResultatType.INNVILGET);
        repoRule.getRepository().lagre(vedtak.getBehandlingsresultat());

        Behandling revurdering = ScenarioMorSøkerEngangsstønad.forDefaultAktør(false)
            .lagre(repositoryProvider);
        when(behandlingsoppretter.opprettRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER)).thenReturn(revurdering);

        // simulere at den tidligere behandlingen er avsluttet
        behandling.avsluttBehandling();
        Long fagsakId = behandling.getFagsakId();
        DokumentTypeId dokumentTypeEndringssøknad = DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;

        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeEndringssøknad, fagsakId, "", now(), true, null);

        //Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeEndringssøknad, BehandlingÅrsakType.UDEFINERT);//Behandlingårsaktype blir aldri satt for mottatt dokument, så input er i udefinert.

        //Assert
        verify(behandlingsoppretter).opprettRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER); //Skal ved opprettelse av revurdering fra endringssøknad sette behandlingårsaktype til 'endring fra bruker'.
        verify(dokumentmottakerFelles).opprettHistorikk(any(Behandling.class), eq(mottattDokument.getJournalpostId()));
    }

    @Test
    public void skal_oppdatere_behandling_med_endringssøknad_dersom_siste_behandling_er_åpen() {
        //Arrange
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forDefaultAktør(false)
            .lagre(repositoryProvider);

        Long fagsakId = behandling.getFagsakId();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;

        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);

        //Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        //Assert
        verify(dokumentmottaker).oppdaterÅpenBehandlingMedDokument(behandling, mottattDokument, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        verify(kompletthetskontroller).persisterDokumentOgVurderKompletthet(behandling, mottattDokument);
        verify(dokumentmottakerFelles).opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Test
    public void skal_dekøe_første_behandling_i_sakskompleks_dersom_endringssøknad_på_endringssøknad() {
        // Arrange - opprette køet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        simulerKøetBehandling(behandling);

        Long fagsakId = behandling.getFagsakId();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;

        // Arrange - mock
        Behandling nyBehandling = mock(Behandling.class);
        when(nyBehandling.getFagsak()).thenReturn(behandling.getFagsak());
        when(behandlingsoppretter.oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER)).thenReturn(nyBehandling);

        // Arrange - legg inn endringssøknad i mottatte dokumenter
        when(mottatteDokumentTjeneste.harMottattDokumentSet(behandling.getId(), DokumentTypeId.getEndringSøknadTyper())).thenReturn(true);

        //Act - bygg søknad
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        //Assert
        verify(dokumentmottaker).oppdaterÅpenBehandlingMedDokument(any(Behandling.class), eq(mottattDokument), eq(BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER));
        verify(behandlingsoppretter).oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        verify(køKontroller).dekøFørsteBehandlingISakskompleks(nyBehandling);
    }

    @Test
    public void skal_opprette_køet_behandling_og_kjøre_kompletthet_dersom_køet_behandling_ikke_finnes() {
        // Arrange - opprette fagsak uten behandling
        Fagsak fagsak = DokumentmottakTestUtil.byggFagsak(new AktørId("1"), new Saksnummer("123"), fagsakRepository);

        // Arrange - mock tjenestekall
        Behandling behandling = mock(Behandling.class);
        long behandlingId = 1L;
        doReturn(behandlingId).when(behandling).getId();
        when(behandlingsoppretter.opprettKøetBehandling(fagsak, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER)).thenReturn(behandling);
        doNothing().when(kompletthetskontroller).vurderKompletthetForKøetBehandling(behandling);

        // Act - send inn endringssøknad
        Long fagsakId = fagsak.getId();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);
        dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, fagsak, DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        // Assert - verifiser flyt
        verify(behandlingsoppretter).opprettKøetBehandling(fagsak, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
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

        // Arrange - mock tjenestekall
        doNothing().when(kompletthetskontroller).vurderKompletthetForKøetBehandling(behandling);

        // Act - send inn endringssøknad
        Long fagsakId = behandling.getFagsakId();
        Fagsak fagsak = behandling.getFagsak();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);
        dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, fagsak, DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        // Assert - verifiser flyt
        verify(kompletthetskontroller).persisterKøetDokumentOgVurderKompletthet(behandling, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Test
    public void skal_henlegge_køet_behandling_dersom_endringssøknad_mottatt_tidligere() {
        // Arrange - opprette køet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        simulerKøetBehandling(behandling);

        // Arrange - mock tjenestekall
        Behandling nyKøetBehandling = mock(Behandling.class);
        when(nyKøetBehandling.getFagsak()).thenReturn(behandling.getFagsak());

        when(mottatteDokumentTjeneste.harMottattDokumentSet(behandling.getId(), DokumentTypeId.getEndringSøknadTyper())).thenReturn(true);
        when(behandlingsoppretter.oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.KØET_BEHANDLING)).thenReturn(nyKøetBehandling);

        // Arrange - bygg søknad
        Long fagsakId = behandling.getFagsakId();
        Fagsak fagsak = behandling.getFagsak();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);

        // Act
        dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, fagsak, DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        // Assert - verifiser flyt
        verify(behandlingsoppretter).oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.KØET_BEHANDLING);
        verify(kompletthetskontroller).persisterKøetDokumentOgVurderKompletthet(nyKøetBehandling, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikk(behandling, mottattDokument.getJournalpostId());
    }

    @Test
    public void skal_oppdatere_mottatt_dokument_med_behandling_hvis_behandlig_er_på_vent() {
        //Arrange
        Behandling behandling = ScenarioMorSøkerForeldrepenger
            .forDefaultAktør()
            .lagre(repositoryProvider);

        Long fagsakId = behandling.getFagsakId();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;

        MottattDokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);

        //Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        //Assert
        verify(mottatteDokumentTjeneste).oppdaterMottattDokumentMedBehandling(mottattDokument, behandling.getId());
    }

    private void simulerKøetBehandling(Behandling behandling) {
        BehandlingÅrsakType berørtType = kodeverkRepository.finn(BehandlingÅrsakType.class, BehandlingÅrsakType.KØET_BEHANDLING);
        new BehandlingÅrsak.Builder(Arrays.asList(berørtType)).buildFor(behandling);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING);
    }

    private Fagsak nyMorFødselFagsak() {
        return ScenarioMorSøkerEngangsstønad.forDefaultAktør(false).lagreFagsak(repositoryProvider);
    }
}
