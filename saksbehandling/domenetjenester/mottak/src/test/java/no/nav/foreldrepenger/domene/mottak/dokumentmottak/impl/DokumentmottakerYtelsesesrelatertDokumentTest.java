package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.HistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.Whitebox;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;

@RunWith(CdiRunner.class)
public class DokumentmottakerYtelsesesrelatertDokumentTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    @Mock
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    @Mock
    private DokumentmottakerFelles dokumentmottakerFelles;
    @Mock
    private Kompletthetskontroller kompletthetskontroller;
    @Mock
    private Behandlingsoppretter behandlingsoppretter;
    @Mock
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    private DokumentmottakerYtelsesesrelatertDokument dokumentmottaker;

    @Before
    public void oppsett() {
        MockitoAnnotations.initMocks(this);

        dokumentmottakerFelles = new DokumentmottakerFelles(prosessTaskRepository, behandlendeEnhetTjeneste, historikkinnslagTjeneste);

        dokumentmottakerFelles = Mockito.spy(dokumentmottakerFelles);

        dokumentmottaker = new DokumentmottakerSøknad(repositoryProvider, dokumentmottakerFelles, mottatteDokumentTjeneste,
            behandlingsoppretter, kompletthetskontroller);

        dokumentmottaker = Mockito.spy(dokumentmottaker);

        OrganisasjonsEnhet enhet = new OrganisasjonsEnhet("0312", "enhetNavn");
        when(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(any(Fagsak.class))).thenReturn(enhet);
        when(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(any(Behandling.class))).thenReturn(enhet);
    }

    @Test
    public void skal_opprette_vurder_dokument_oppgave_dersom_avvist_behandling() {
        // Arrange - opprette avsluttet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medVilkårResultatType(VilkårResultatType.AVSLÅTT);
        Behandling behandling = scenario.lagre(repositoryProvider);
        scenario.avsluttBehandling(repositoryProvider, behandling);

        BehandlingVedtak vedtak = DokumentmottakTestUtil.oppdaterVedtaksresultat(behandling, VedtakResultatType.AVSLAG);
        Whitebox.setInternalState(behandling.getBehandlingsresultat(), "behandlingVedtak", vedtak);
        repository.lagre(behandling.getBehandlingsresultat());

        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        InngåendeSaksdokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "123");
        when(behandlingsoppretter.erAvslåttFørstegangsbehandling(behandling)).thenReturn(true);

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);

        // Assert
        verify(dokumentmottaker).håndterAvslåttBehandling(mottattDokument, behandling.getFagsak(), behandling);
        verify(dokumentmottakerFelles).opprettTaskForÅVurdereDokument(behandling.getFagsak(), behandling, mottattDokument);
    }

    @Test
    public void skal_opprette_førstegangsbehandling_dersom_avvist_behandling_har_entydig_avslag() {
        // Arrange - opprette avsluttet førstegangsbehandling
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        scenario.medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.IKKE_OPPFYLT);
        Behandling behandling = scenario.lagre(repositoryProvider);
        scenario.avsluttBehandling(repositoryProvider, behandling);
        
        BehandlingVedtak vedtak = DokumentmottakTestUtil.oppdaterVedtaksresultat(behandling, VedtakResultatType.AVSLAG);
        Whitebox.setInternalState(behandling.getBehandlingsresultat(), "behandlingVedtak", vedtak);
        repository.lagre(behandling.getBehandlingsresultat());

        DokumentTypeId dokumentTypeId = DokumentTypeId.LEGEERKLÆRING;
        InngåendeSaksdokument mottattDokument = DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "123");
        doReturn(true).when(behandlingsoppretter).erAvslåttFørstegangsbehandling(behandling);

        ScenarioMorSøkerForeldrepenger morSøkerForeldrepenger = ScenarioMorSøkerForeldrepenger.forAktør(behandling.getAktørId());
        morSøkerForeldrepenger.medFagsakId(behandling.getFagsakId());
        Behandling nyBehandling = morSøkerForeldrepenger.lagre(repositoryProvider);
        doReturn(nyBehandling).when(behandlingsoppretter).finnEllerOpprettFørstegangsbehandling(behandling.getFagsak());

        // Act
        dokumentmottaker.mottaDokument(mottattDokument, behandling.getFagsak(), dokumentTypeId, BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);

        // Assert
        verify(dokumentmottaker).håndterAvslåttBehandling(mottattDokument, behandling.getFagsak(), behandling);
    }
}
