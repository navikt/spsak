package no.nav.foreldrepenger.sak.v1;

import static java.lang.Long.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakFeil;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakOrchestrator;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakTjeneste;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.OpprettSakUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.meldinger.OpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.meldinger.OpprettSakResponse;
import no.nav.vedtak.felles.testutilities.Whitebox;

public class OpprettSakServiceTest {

    private OpprettSakService service;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private OpprettSakTjeneste opprettSakTjeneste;
    @Mock
    private FagsakRepository fagsakRepository;
    @Mock
    private JournalTjeneste journalTjeneste;
    private KodeverkRepository kodeverkRepository;
    private OpprettSakOrchestrator opprettSakOrchestrator;

    private final String JOURNALPOST = "1234";
    private final JournalpostId JOURNALPOST_ID = new JournalpostId(JOURNALPOST);

    @Before
    public void before() {
        opprettSakTjeneste = mock(OpprettSakTjeneste.class);
        journalTjeneste = mock(JournalTjeneste.class);
        fagsakRepository = mock(FagsakRepository.class);
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        opprettSakOrchestrator = new OpprettSakOrchestrator(opprettSakTjeneste, fagsakRepository);
        service = new OpprettSakService(opprettSakOrchestrator, kodeverkRepository, journalTjeneste);
    }

    @Test(expected = OpprettSakUgyldigInput.class)
    public void test_feilhandering_mangler_journaposId() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(null, "7890", "ab0050");
        service.opprettSak(request);
    }

    @Test(expected = OpprettSakUgyldigInput.class)
    public void test_feilhandering_mangler_behandlingstema() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", null);
        service.opprettSak(request);
    }

    @Test(expected = OpprettSakUgyldigInput.class)
    public void test_feilhandering_ukjent_behandlingstema() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "xx1234");
        service.opprettSak(request);
    }

    @Test(expected = OpprettSakUgyldigInput.class)
    public void test_feilhandering_mangler_aktorId() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, null, "ab0050");
        service.opprettSak(request);
    }

    @Test(expected = OpprettSakUgyldigInput.class)
    public void test_opprettSak_finner_ikke_bruker() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0050");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        when(opprettSakTjeneste.opprettSakVL(eq(aktorIdLong), any(BehandlingTema.class), any(JournalpostId.class))).thenThrow(OpprettSakFeil.FACTORY.finnerIkkePersonMedAktørId(aktorIdLong).toException());

        service.opprettSak(request);
    }

    @Test
    public void test_opprettSak_ok_fødsel() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0050");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        final Long FAGSAKID = 1l;
        final Saksnummer expectedSakId = new Saksnummer("2");

        Fagsak fagsak = mockFagsak(FAGSAKID);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD_FØDSEL, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktorIdLong)).thenReturn(expectedSakId);
        mockOppdaterFagsakMedGsakId(fagsak, expectedSakId);
        when(journalTjeneste.hentInngåendeJournalpostHoveddokument(any(), any())).thenReturn(ArkivJournalPost.Builder.ny().medJournalpostId(JOURNALPOST_ID)
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentKategori(DokumentKategori.UDEFINERT).medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build()).build());

        OpprettSakResponse response = service.opprettSak(request);
        assertThat(response.getSakId()).as("Forventer at saksnummer blir returnert ut fra tjenesten.").isEqualTo(expectedSakId.getVerdi());
    }

    @Test
    public void test_opprettSak_ok_fødsel_udefinert_doktypesatt() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0050");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        final Long FAGSAKID = 1l;
        final Saksnummer expectedSakId = new Saksnummer("2");

        Fagsak fagsak = mockFagsak(FAGSAKID);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD_FØDSEL, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktorIdLong)).thenReturn(expectedSakId);
        when(fagsakRepository.hentJournalpost(any())).thenReturn(Optional.empty());
        when(journalTjeneste.hentInngåendeJournalpostHoveddokument(any(), any())).thenReturn(ArkivJournalPost.Builder.ny().medJournalpostId(JOURNALPOST_ID)
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentKategori(DokumentKategori.SØKNAD).medDokumentTypeId(DokumentTypeId.UDEFINERT).build()).build());
        mockOppdaterFagsakMedGsakId(fagsak, expectedSakId);

        // Act
        OpprettSakResponse response = service.opprettSak(request);

        ArgumentCaptor<BehandlingTema> captor = ArgumentCaptor.forClass(BehandlingTema.class);
        verify(opprettSakTjeneste, times(1)).opprettSakVL(any(AktørId.class), captor.capture(), any(JournalpostId.class));
        BehandlingTema bt = captor.getValue();
        assertThat(bt).isEqualTo(BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        assertThat(response.getSakId()).as("Forventer at saksnummer blir returnert ut fra tjenesten.").isEqualTo(expectedSakId.getVerdi());
    }

    @Test(expected = OpprettSakUgyldigInput.class)
    public void test_opprettSak_unntak_klagedokument() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0050");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        final Long FAGSAKID = 1l;
        final Saksnummer expectedSakId = new Saksnummer("2");

        Fagsak fagsak = mockFagsak(FAGSAKID);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD_FØDSEL, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktorIdLong)).thenReturn(expectedSakId);
        when(fagsakRepository.hentJournalpost(Matchers.any(JournalpostId.class))).thenReturn(Optional.empty());
        when(journalTjeneste.hentInngåendeJournalpostHoveddokument(any(), any())).thenReturn(ArkivJournalPost.Builder.ny().medJournalpostId(JOURNALPOST_ID)
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentKategori(DokumentKategori.UDEFINERT).medDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT).build()).build());

        service.opprettSak(request);
    }

    @Test
    public void test_opprettSak_ok_adopsjon() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0027");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        final Long FAGSAKID = 1l;
        final Saksnummer expectedSakId = new Saksnummer("2");

        Fagsak fagsak = mockFagsak(FAGSAKID);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD_ADOPSJON, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktorIdLong)).thenReturn(expectedSakId);
        mockOppdaterFagsakMedGsakId(fagsak, expectedSakId);
        when(journalTjeneste.hentInngåendeJournalpostHoveddokument(any(), any())).thenReturn(ArkivJournalPost.Builder.ny().medJournalpostId(JOURNALPOST_ID)
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentKategori(DokumentKategori.UDEFINERT).medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON).build()).build());

        OpprettSakResponse response = service.opprettSak(request);
        assertThat(response.getSakId()).as("Forventer at saksnummer blir returnert ut fra tjenesten.").isEqualTo(expectedSakId.getVerdi());
    }

    @Test(expected = OpprettSakUgyldigInput.class)
    public void test_opprettSak_unntak_klageelleramnnke() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0050");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        final Long FAGSAKID = 1l;
        final Saksnummer expectedSakId = new Saksnummer("2");

        Fagsak fagsak = mockFagsak(FAGSAKID);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD_FØDSEL, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktorIdLong)).thenReturn(expectedSakId);
        when(fagsakRepository.hentJournalpost(Matchers.any(JournalpostId.class))).thenReturn(Optional.empty());
        when(journalTjeneste.hentInngåendeJournalpostHoveddokument(any(), any())).thenReturn(ArkivJournalPost.Builder.ny().medJournalpostId(JOURNALPOST_ID)
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentKategori(DokumentKategori.KLAGE_ELLER_ANKE).medDokumentTypeId(DokumentTypeId.UDEFINERT).build()).build());

        service.opprettSak(request);
    }

    @Test
    public void test_opprettSak_ok_annen_engangsstønad() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0327");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        final Long FAGSAKID = 1l;
        final Saksnummer expectedSakId = new Saksnummer("2");

        Fagsak fagsak = mockFagsak(FAGSAKID);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD_FØDSEL, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktorIdLong)).thenReturn(expectedSakId);
        mockOppdaterFagsakMedGsakId(fagsak, expectedSakId);
        when(journalTjeneste.hentInngåendeJournalpostHoveddokument(any(), any())).thenReturn(ArkivJournalPost.Builder.ny().medJournalpostId(JOURNALPOST_ID)
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentKategori(DokumentKategori.SØKNAD).medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build()).build());


        OpprettSakResponse response = service.opprettSak(request);
        assertThat(response.getSakId()).as("Forventer at saksnummer blir returnert ut fra tjenesten.").isEqualTo(expectedSakId.getVerdi());
    }

    @Test
    public void test_opprettSak_ok_annen_engangsstønad_doktypesatt() throws Exception {
        OpprettSakRequest request = createOpprettSakRequest(JOURNALPOST, "7890", "ab0327");
        AktørId aktorIdLong = new AktørId(valueOf(request.getSakspart().getAktoerId()));

        final Long FAGSAKID = 1l;
        final Saksnummer expectedSakId = new Saksnummer("2");

        Fagsak fagsak = mockFagsak(FAGSAKID);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD_FØDSEL, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakVL(aktorIdLong, BehandlingTema.ENGANGSSTØNAD, JOURNALPOST_ID)).thenReturn(fagsak);
        when(opprettSakTjeneste.opprettSakIGsak(fagsak.getId(), aktorIdLong)).thenReturn(expectedSakId);
        when(fagsakRepository.hentJournalpost(any())).thenReturn(Optional.empty());
        when(journalTjeneste.hentInngåendeJournalpostHoveddokument(any(), any())).thenReturn(ArkivJournalPost.Builder.ny().medJournalpostId(JOURNALPOST_ID)
            .medHoveddokument(ArkivDokument.Builder.ny().medDokumentKategori(DokumentKategori.UDEFINERT).medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build()).build());

        mockOppdaterFagsakMedGsakId(fagsak, expectedSakId);

        // Act
        OpprettSakResponse response = service.opprettSak(request);

        ArgumentCaptor<BehandlingTema> captor = ArgumentCaptor.forClass(BehandlingTema.class);
        verify(opprettSakTjeneste, times(1)).opprettSakVL(any(AktørId.class), captor.capture(), any(JournalpostId.class));
        BehandlingTema bt = captor.getValue();
        assertThat(bt).isEqualTo(BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        assertThat(response.getSakId()).as("Forventer at saksnummer blir returnert ut fra tjenesten.").isEqualTo(expectedSakId.getVerdi());

    }

    private void mockOppdaterFagsakMedGsakId(Fagsak fagsak, Saksnummer sakId) {
        doAnswer(invocationOnMock -> { Whitebox.setInternalState(fagsak, "saksnummer", sakId); return null; })
            .when(opprettSakTjeneste).oppdaterFagsakMedGsakSaksnummer(anyLong(), any(Saksnummer.class));

    }


    private Fagsak mockFagsak(Long fagsakId) {
        Fagsak fagsak = mock(Fagsak.class);
        when(fagsak.getId()).thenReturn(fagsakId);
        Whitebox.setInternalState(fagsak, "saksnummer", null);
        when(fagsak.getSaksnummer()).thenReturn((Saksnummer) Whitebox.getInternalState(fagsak, "saksnummer"));
        return fagsak;
    }

    private OpprettSakRequest createOpprettSakRequest(String journalpostId, String aktorId, String behandlingstema) {
        OpprettSakRequest request = new OpprettSakRequest();
        request.setJournalpostId(journalpostId);
        Behandlingstema behTema = new Behandlingstema();
        behTema.setValue(behandlingstema);
        request.setBehandlingstema(behTema);
        Aktoer aktoer = new Aktoer();
        aktoer.setAktoerId(aktorId);
        request.setSakspart(aktoer);
        return request;
    }
}
