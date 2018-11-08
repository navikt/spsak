package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.HåndterMottattDokumentTaskProperties;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEventPubliserer;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class OpprettNyFørstegangsbehandlingTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private Behandling behandling;

    private BehandlingRepositoryProvider repositoryProvider;

    private DatavarehusTjeneste datavarehusTjeneste;

    private OppgaveTjeneste oppgaveTjeneste;
    private ProsessTaskRepository prosessTaskRepository;
    private SaksbehandlingDokumentmottakTjeneste saksbehandlingDokumentmottakTjeneste;
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    private KodeverkRepository kodeverkRepository;

    private Behandling opprettOgLagreBehandling() {
        return ScenarioMorSøkerEngangsstønad.forFødsel().lagre(repositoryProvider);
    }

    @Before
    public void setup() {
        ProsessTaskEventPubliserer prosessTaskEventPubliserer = Mockito.mock(ProsessTaskEventPubliserer.class);
        Mockito.doNothing().when(prosessTaskEventPubliserer).fireEvent(Mockito.any(ProsessTaskData.class), Mockito.any(), Mockito.any(), Mockito.any());
        oppgaveTjeneste = mock(OppgaveTjeneste.class);
        prosessTaskRepository = Mockito.spy(new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), prosessTaskEventPubliserer));
        datavarehusTjeneste = mock(DatavarehusTjeneste.class);
        MottatteDokumentTjeneste mottatteDokumentTjeneste = mock(MottatteDokumentTjeneste.class);

        repositoryProvider = Mockito.spy(new BehandlingRepositoryProviderImpl(repoRule.getEntityManager()));
        kodeverkRepository = repositoryProvider.getKodeverkRepository();
        behandling = opprettOgLagreBehandling();
        mockMottatteDokumentRepository(repositoryProvider, mottatteDokumentTjeneste);

        saksbehandlingDokumentmottakTjeneste = new SaksbehandlingDokumentmottakTjenesteImpl(prosessTaskRepository, mottatteDokumentTjeneste);

        behandlingsutredningApplikasjonTjeneste = new BehandlingsutredningApplikasjonTjenesteImpl(
            Period.parse("P4W"),
            repositoryProvider,
            null,
            oppgaveTjeneste,
            null,
            null,
            saksbehandlingDokumentmottakTjeneste,
            datavarehusTjeneste,
            null);
    }

    private void mockMottatteDokumentRepository(BehandlingRepositoryProvider repositoryProvider, MottatteDokumentTjeneste mottatteDokumentTjeneste) {
        MottatteDokumentRepository mottatteDokumentRepository = mock(MottatteDokumentRepository.class);
        when(mottatteDokumentRepository.hentMottatteDokumentMedFagsakId(behandling.getFagsakId())).thenAnswer(invocation -> {
            List<MottattDokument> mottatteDokumentList = new ArrayList<>();
            MottattDokument md1 = new MottattDokument.Builder()
                .medBehandlingId(behandling.getId())
                .medJournalPostId(new JournalpostId("123"))
                .medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON)
                .medMottattDato(LocalDate.now())
                .medElektroniskRegistrert(true)
                .medFagsakId(behandling.getFagsakId())
                .build();
            Whitebox.setInternalState(md1, "opprettetTidspunkt", LocalDateTime.now().minusSeconds(1L));
            mottatteDokumentList.add(md1);
            MottattDokument md2 = new MottattDokument.Builder() //Annet dokument som ikke er søknad
                .medBehandlingId(behandling.getId())
                .medJournalPostId(new JournalpostId("123"))
                .medDokumentTypeId(DokumentTypeId.UDEFINERT)
                .medMottattDato(LocalDate.now())
                .medElektroniskRegistrert(true)
                .medFagsakId(behandling.getFagsakId())
                .build();
            Whitebox.setInternalState(md2, "opprettetTidspunkt", LocalDateTime.now().minusSeconds(1L));
            mottatteDokumentList.add(md2);
            MottattDokument md3 = new MottattDokument.Builder()
                .medBehandlingId(behandling.getId())
                .medJournalPostId(new JournalpostId("123"))
                .medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL)
                .medMottattDato(LocalDate.now())
                .medElektroniskRegistrert(true)
                .medFagsakId(behandling.getFagsakId())
                .build();
            Whitebox.setInternalState(md3, "opprettetTidspunkt", LocalDateTime.now());
            mottatteDokumentList.add(md3);
            MottattDokument md4 = new MottattDokument.Builder()
                .medBehandlingId(behandling.getId())
                .medJournalPostId(new JournalpostId("123"))
                .medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL)
                .medMottattDato(LocalDate.now())
                .medElektroniskRegistrert(false)
                .medXmlPayload("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>") // Skal bare være en string slik at XmlPayLoad ikke er null
                .medFagsakId(behandling.getFagsakId())
                .build();
            Whitebox.setInternalState(md4, "opprettetTidspunkt", LocalDateTime.now().plusSeconds(1L));
            mottatteDokumentList.add(md4);

            return mottatteDokumentList;
        });
        when(repositoryProvider.getMottatteDokumentRepository()).thenReturn(mottatteDokumentRepository);

        saksbehandlingDokumentmottakTjeneste = new SaksbehandlingDokumentmottakTjenesteImpl(prosessTaskRepository, mottatteDokumentTjeneste);

        behandlingsutredningApplikasjonTjeneste = new BehandlingsutredningApplikasjonTjenesteImpl(
            Period.parse("P4W"),
            repositoryProvider,
            null,
            oppgaveTjeneste,
            null,
            null,
            saksbehandlingDokumentmottakTjeneste,
            datavarehusTjeneste,
            null);

    }

    @Test
    public void skal_opprette_nyførstegangsbehandling() {
        //Arrange
        behandling.avsluttBehandling();

        //Act
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(behandling.getFagsakId(), behandling.getFagsak().getSaksnummer(), false);

        //Assert
        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskRepository, times(1)).lagre(captor.capture());
        ProsessTaskData prosessTaskData = captor.getValue();
        verifiserProsessTaskData(behandling, prosessTaskData);

    }

    @Test(expected = FunksjonellException.class)
    public void skal_kaste_exception_når_behandling_fortsatt_er_åpen() {
        //Act and expect Exception
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(behandling.getFagsakId(), behandling.getFagsak().getSaksnummer(), false);
    }

    @Test(expected = FunksjonellException.class)
    public void skal_kaste_exception_når_behandling_ikke_eksisterer() {
        //Act and expect Exception
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(-1L, new Saksnummer("50"), false);
    }

    @Test
    public void skal_opprette_etter_klagebehandling() {
        //Arrange
        behandling.avsluttBehandling();

        Behandling klage = Behandling.forKlage(behandling.getFagsak()).build();
        klage.leggTilKlageVurderingResultat(KlageVurderingResultat.builder()
            .medKlageVurdertAv(KlageVurdertAv.NFP).medKlageMedholdÅrsak(KlageMedholdÅrsak.NYE_OPPLYSNINGER).medKlageVurdering(KlageVurdering.MEDHOLD_I_KLAGE)
            .medBegrunnelse("bla bla").medVedtaksdatoPåklagdBehandling(LocalDate.now()).medBehandling(klage).build());
        klage.avsluttBehandling();
        repositoryProvider.getBehandlingRepository().lagre(klage, repositoryProvider.getBehandlingRepository().taSkriveLås(klage));

        //Act
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(behandling.getFagsakId(), behandling.getFagsak().getSaksnummer(),true);

        // Assert
        ArgumentCaptor<ProsessTaskData> captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskRepository, times(1)).lagre(captor.capture());
        ProsessTaskData prosessTaskData = captor.getValue();
        verifiserProsessTaskData(behandling, prosessTaskData);
    }

    @Test(expected = FunksjonellException.class)
    public void skal_feile_uten_tidligere_klagebehandling() {
        //Arrange
        behandling.avsluttBehandling();

        //Act
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(behandling.getFagsakId(), behandling.getFagsak().getSaksnummer(),true);
    }

    // Gjennomgå om man skal forby oppretting av NyFB basert på henlagt klage
    @Ignore
    @Test(expected = FunksjonellException.class)
    public void skal_feile_henlagt_tidligere_klagebehandling() {
        //Arrange
        behandling.avsluttBehandling();

        Behandling klage = Behandling.forKlage(behandling.getFagsak()).build();
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.HENLAGT_KLAGE_TRUKKET).buildFor(klage);
        klage.avsluttBehandling();
        repositoryProvider.getBehandlingRepository().lagre(klage, repositoryProvider.getBehandlingRepository().taSkriveLås(klage));

        //Act
        behandlingsutredningApplikasjonTjeneste.opprettNyFørstegangsbehandling(behandling.getFagsakId(), behandling.getFagsak().getSaksnummer(),true);
    }

    //Verifiserer at den opprettede prosesstasken stemmer overens med MottattDokument-mock
    private void verifiserProsessTaskData(Behandling behandling, ProsessTaskData prosessTaskData) {
        final BehandlingTema behandlingTemaFødsel = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        assertThat(prosessTaskData.getTaskType()).isEqualTo(HåndterMottattDokumentTaskProperties.TASKTYPE);
        assertThat(prosessTaskData.getFagsakId()).isEqualTo(behandling.getFagsakId());
        assertThat(prosessTaskData.getPropertyValue(HåndterMottattDokumentTaskProperties.BEHANDLINGSTEMA_OFFISIELL_KODE_KEY))
            .isEqualTo(behandlingTemaFødsel.getOffisiellKode());
    }
}
