package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class DokumentmottakerEndringssøknadTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Inject
    private GrunnlagRepositoryProvider repositoryProvider;
    @Inject
    private ResultatRepositoryProvider resultatRepositoryProvider;
    @Inject
    private BehandlingRepository behandlingRepository;
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

        InngåendeSaksdokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeEndringssøknad, fagsakId, "", now(), true, null);

        //Act
        dokumentmottaker.mottaDokument(mottattDokument, fagsak, dokumentTypeEndringssøknad, BehandlingÅrsakType.UDEFINERT);

        //Assert
        verify(dokumentmottakerFelles).opprettTaskForÅVurdereDokument(fagsak, null, mottattDokument);
    }

    @Test
    public void skal_opprette_revurdering_for_endringssøknad_dersom_siste_behandling_er_avsluttet() {
        //Arrange
        Behandling behandling = ScenarioMorSøkerEngangsstønad.forDefaultAktør(false)
            .lagre(repositoryProvider, resultatRepositoryProvider);
        BehandlingVedtak vedtak = oppdaterVedtaksresultat(VedtakResultatType.INNVILGET, behandlingRepository.hentResultat(behandling.getId()));
        repoRule.getRepository().lagre(vedtak.getBehandlingsresultat());

        Behandling revurdering = ScenarioMorSøkerEngangsstønad.forDefaultAktør(false)
            .lagre(repositoryProvider, resultatRepositoryProvider);
        when(behandlingsoppretter.opprettRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER)).thenReturn(revurdering);

        // simulere at den tidligere behandlingen er avsluttet
        avsluttBehandling(behandling);
        Long fagsakId = behandling.getFagsakId();
        DokumentTypeId dokumentTypeEndringssøknad = DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;

        InngåendeSaksdokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeEndringssøknad, fagsakId, "", now(), true, null);

        //Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeEndringssøknad, BehandlingÅrsakType.UDEFINERT);//Behandlingårsaktype blir aldri satt for mottatt dokument, så input er i udefinert.

        //Assert
        verify(behandlingsoppretter).opprettRevurdering(behandling.getFagsak(), BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER); //Skal ved opprettelse av revurdering fra endringssøknad sette behandlingårsaktype til 'endring fra bruker'.
        verify(dokumentmottakerFelles).opprettHistorikk(any(Behandling.class), eq(mottattDokument.getJournalpostId()));
    }

    @SuppressWarnings("deprecation")
    private void avsluttBehandling(Behandling behandling) {
        Whitebox.setInternalState(behandling, "status", BehandlingStatus.AVSLUTTET);
    }

    @Test
    public void skal_dekøe_første_behandling_i_sakskompleks_dersom_endringssøknad_på_endringssøknad() {
        // Arrange - opprette køet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        simulerKøetBehandling(behandling);

        Long fagsakId = behandling.getFagsakId();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;

        // Arrange - mock
        Behandling nyBehandling = mock(Behandling.class);
        when(nyBehandling.getFagsak()).thenReturn(behandling.getFagsak());
        when(behandlingsoppretter.oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER)).thenReturn(nyBehandling);

        //Act - bygg søknad
        InngåendeSaksdokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        //Assert
        verify(dokumentmottaker).oppdaterÅpenBehandlingMedDokument(any(Behandling.class), eq(mottattDokument), eq(BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER));
        verify(behandlingsoppretter).oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        verify(køKontroller).dekøFørsteBehandlingISakskompleks(nyBehandling);
    }

    @Test
    public void skal_henlegge_køet_behandling_dersom_endringssøknad_mottatt_tidligere() {
        // Arrange - opprette køet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        simulerKøetBehandling(behandling);

        // Arrange - mock tjenestekall
        Behandling nyKøetBehandling = mock(Behandling.class);
        when(nyKøetBehandling.getFagsak()).thenReturn(behandling.getFagsak());

        when(behandlingsoppretter.oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.KØET_BEHANDLING)).thenReturn(nyKøetBehandling);

        // Arrange - bygg søknad
        Long fagsakId = behandling.getFagsakId();
        Fagsak fagsak = behandling.getFagsak();
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        InngåendeSaksdokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, fagsakId, "", now(), true, null);

        // Act
        dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, fagsak, DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);

        // Assert - verifiser flyt
        verify(behandlingsoppretter).oppdaterBehandlingViaHenleggelse(behandling, BehandlingÅrsakType.KØET_BEHANDLING);
        verify(kompletthetskontroller).persisterKøetDokumentOgVurderKompletthet(nyKøetBehandling, mottattDokument, Optional.empty());
        verify(dokumentmottakerFelles).opprettHistorikk(behandling, mottattDokument.getJournalpostId());
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

    private static BehandlingVedtak oppdaterVedtaksresultat(VedtakResultatType vedtakResultatType, Behandlingsresultat behandlingsresultat) {
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(vedtakResultatType)
            .medVedtaksdato(LocalDate.now())
            .medBehandlingsresultat(behandlingsresultat)
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .build();

        return vedtak;
    }
}
