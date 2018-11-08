package no.nav.foreldrepenger.sak.tjeneste;
// NOSONAR
/* TODO(humle): Fjerne eller fikse
public class OpprettSakTjenesteTest {

    private static final Long FAGSAK_ID = 123L;
    @Mock
    private TpsTjeneste tpsTjenesteMock;
    @Mock
    private BehandleSakConsumer behandleSakConsumerMock;
    private FagsakTjeneste fagsakTjenesteMock;

    @Mock
    private BrukerTjeneste brukerTjeneste;

    private Optional<Personinfo> personinfo;

    OpprettSakTjeneste opprettSakTjeneste;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        personinfo = Optional.of(new Personinfo.Builder()
                .medAktørId(new AktørId("123"))
                .medFnr("07078515478")
                .medNavn("Kari Nordmann")
                .medFødselsdato(LocalDate.of(1985, 7, 7))
                .medNavBrukerKjønn(KVINNE)
                .build());

        behandleSakConsumerMock = mock(BehandleSakConsumer.class);
        tpsTjenesteMock = mock(TpsTjeneste.class);
        fagsakTjenesteMock = new FagsakTjeneste(mock(BehandlingRepositoryProvider.class), null) {
            @Override
            public void opprettFagsak(Fagsak nyFagsak, Personinfo foreldersPersoninfo) {
                nyFagsak.setId(FAGSAK_ID);
            }
        };
        brukerTjeneste = mock(BrukerTjeneste.class);

        opprettSakTjeneste = new OpprettSakTjenesteImpl(tpsTjenesteMock, fagsakTjenesteMock, behandleSakConsumerMock, brukerTjeneste);
    }

    @Test
    public void test_opprettSakIVedtakslosningen_engangsstønadAdopsjon() throws Exception {
        AktørId aktorId = new AktørId("12345");

        when(tpsTjenesteMock.hentBrukerForAktør(anyLong())).thenReturn(personinfo);
        when(brukerTjeneste.hentEllerOpprettFraAktorId(any())).thenReturn(NavBruker.opprettNy(personinfo.get()));

        Fagsak fagsak = opprettSakTjeneste.opprettSakVL(aktorId, BehandlingTema.ENGANGSSTØNAD_ADOPSJON);

        assertThat(FAGSAK_ID).as("Forventer at fakgsak returnert fra metode er den osm ble opprettet").isEqualTo(fagsak.getId());
    }

    @Test
    public void test_opprettSakIVedtakslosningen_engangsstønad() throws Exception {
        AktørId aktorId = new AktørId("12345");

        when(tpsTjenesteMock.hentBrukerForAktør(anyLong())).thenReturn(personinfo);
        when(brukerTjeneste.hentEllerOpprettFraAktorId(any())).thenReturn(NavBruker.opprettNy(personinfo.get()));
        Fagsak fagsak = opprettSakTjeneste.opprettSakVL(aktorId, BehandlingTema.ENGANGSSTØNAD);

        assertThat(FAGSAK_ID).as("Forventer at fakgsak returnert fra metode er den osm ble opprettet").isEqualTo(fagsak.getId());
    }

    @Test
    public void test_opprettSakIVedtakslosningen_engangsstønadFødsel() throws Exception {
        AktørId aktorId = new AktørId("12345");

        when(tpsTjenesteMock.hentBrukerForAktør(anyLong())).thenReturn(personinfo);
        when(brukerTjeneste.hentEllerOpprettFraAktorId(any())).thenReturn(NavBruker.opprettNy(personinfo.get()));
        Fagsak fagsak = opprettSakTjeneste.opprettSakVL(aktorId, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        assertThat(FAGSAK_ID).as("Forventer at fakgsak returnert fra metode er den osm ble opprettet").isEqualTo(fagsak.getId());
    }

    @Test
    public void test_opprettSakIVedtakslosningen_annetBehandlingstema() throws Exception {
        AktørId aktorId = new AktørId("12345");

        when(tpsTjenesteMock.hentBrukerForAktør(anyLong())).thenReturn(personinfo);
        when(brukerTjeneste.hentEllerOpprettFraAktorId(any())).thenReturn(NavBruker.opprettNy(personinfo.get()));
        expectedException.expect(TekniskException.class);
        opprettSakTjeneste.opprettSakVL(aktorId, BehandlingTema.UDEFINERT);
    }

    @Test
    public void test_opprettSakIGsak() throws Exception {
        final Long fagsakId = 123L;
        final AktørId aktorId = new AktørId("456");
        final String sakIdExpected = "123456";
        when(tpsTjenesteMock.hentBrukerForAktør(aktorId)).thenReturn(personinfo);

        OpprettSakResponse mockResponse = new OpprettSakResponse();
        mockResponse.setSakId(sakIdExpected);
        when(behandleSakConsumerMock.opprettSak(any())).thenReturn(mockResponse);
        String sakId = opprettSakTjeneste.opprettSakIGsak(fagsakId, aktorId);
        assertThat(sakIdExpected).as("Forvnter at saId returnert fra ekstern tjeneste returneres fra opprettSak tjeneste og.").isEqualTo(sakId);
    }

    @Test
    public void test_oppdaterFagsakMedGsakId() {
        fagsakTjenesteMock = mock(FagsakTjeneste.class);
        opprettSakTjeneste = new OpprettSakTjenesteImpl(tpsTjenesteMock, fagsakTjenesteMock, behandleSakConsumerMock, brukerTjeneste);
        Fagsak fagsak = mock(Fagsak.class);
        opprettSakTjeneste.oppdaterFagsakMedGsakId(fagsak, "1023");

        verify(fagsakTjenesteMock).oppdaterFagsakMedGsakId(eq(fagsak), eq(1023L));
    }

    @Test
    public void test_knyttSakOgJournalpost() {
        final JournalpostId journalpostId = new JournalpostId("1111");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling behandling = scenario.lagMocked();

        fagsakTjenesteMock = mock(FagsakTjeneste.class);
        Fagsak fagsak = behandling.getFagsak();
        Optional<Fagsak> optFagsak = Optional.of(fagsak);
        when(fagsakTjenesteMock.finnFagsak(fagsak.getSaksnummer())).thenReturn(optFagsak);
        when(fagsakTjenesteMock.hentJournalpost(journalpostId)).thenReturn(Optional.empty());

        opprettSakTjeneste = new OpprettSakTjenesteImpl(tpsTjenesteMock, fagsakTjenesteMock, behandleSakConsumerMock, brukerTjeneste);

        opprettSakTjeneste.knyttSakOgJournalpost(""+fagsak.getSaksnummer(), journalpostId);

        verify(fagsakTjenesteMock).lagreJournalPost(eq(new Journalpost(journalpostId, fagsak)));
    }

    @Test
    public void test_knyttSakOgJournalpost_finner_ikke_fagsak() {
        final JournalpostId journalpostId = new JournalpostId("1111");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling behandling = scenario.lagMocked();

        fagsakTjenesteMock = mock(FagsakTjeneste.class);
        Fagsak fagsak = behandling.getFagsak();
        when(fagsakTjenesteMock.hentJournalpost(journalpostId)).thenReturn(Optional.empty());
        when(fagsakTjenesteMock.finnFagsak(fagsak.getSaksnummer())).thenReturn(Optional.empty());

        opprettSakTjeneste = new OpprettSakTjenesteImpl(tpsTjenesteMock, fagsakTjenesteMock, behandleSakConsumerMock, brukerTjeneste);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-840572");
        opprettSakTjeneste.knyttSakOgJournalpost(""+fagsak.getSaksnummer(), journalpostId);
    }

    @Test
    public void test_knyttSakOgJournalpost_knytning_finnes_allerede_mot_samme_sak() {
        final JournalpostId journalpostId = new JournalpostId("1111");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling behandling = scenario.lagMocked();

        fagsakTjenesteMock = mock(FagsakTjeneste.class);
        Fagsak fagsak = behandling.getFagsak();
        Optional<Fagsak> optFagsak = Optional.of(fagsak);
        when(fagsakTjenesteMock.hentJournalpost(journalpostId)).thenReturn(Optional.of(new Journalpost(journalpostId, fagsak))); //Denne indikerer at knytning allerede finnes.

        opprettSakTjeneste = new OpprettSakTjenesteImpl(tpsTjenesteMock, fagsakTjenesteMock, behandleSakConsumerMock, brukerTjeneste);

        opprettSakTjeneste.knyttSakOgJournalpost(""+fagsak.getSaksnummer(), journalpostId);
    }

    @Test
    public void test_knyttSakOgJournalpost_knytning_finnes_allerede_mot_annen_sak() {
        final JournalpostId journalpostId = new JournalpostId("1111");

        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        Behandling behandling = scenario.lagMocked();
        Fagsak fagsak2 = ScenarioMorSøkerEngangsstønad.forFødsel().lagMocked().getFagsak();

        fagsakTjenesteMock = mock(FagsakTjeneste.class);
        Fagsak fagsak = behandling.getFagsak();
        Optional<Fagsak> optFagsak = Optional.of(fagsak);
        when(fagsakTjenesteMock.finnFagsak(fagsak.getSaksnummer())).thenReturn(optFagsak);
        when(fagsakTjenesteMock.hentJournalpost(journalpostId)).thenReturn(Optional.of(new Journalpost(journalpostId, fagsak2))); //Denne indikerer at knytning allerede finnes.

        opprettSakTjeneste = new OpprettSakTjenesteImpl(tpsTjenesteMock, fagsakTjenesteMock, behandleSakConsumerMock, brukerTjeneste);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-863070");
        opprettSakTjeneste.knyttSakOgJournalpost(""+fagsak.getSaksnummer(), journalpostId);
    }

    @Test
    public void skal_ha_0_arg_ctor() {
        new OpprettSakTjenesteImpl();
    }

}
*/
