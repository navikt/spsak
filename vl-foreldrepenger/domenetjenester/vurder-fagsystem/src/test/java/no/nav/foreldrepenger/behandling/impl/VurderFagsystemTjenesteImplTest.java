package no.nav.foreldrepenger.behandling.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType.INNSYN;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType.KLAGE;
import static no.nav.foreldrepenger.behandlingslager.testutilities.behandling.BehandlingslagerTestUtil.buildFagsak;
import static no.nav.foreldrepenger.behandlingslager.testutilities.behandling.BehandlingslagerTestUtil.byggBehandlingFødsel;
import static no.nav.foreldrepenger.behandlingslager.testutilities.behandling.BehandlingslagerTestUtil.byggFødselGrunnlag;
import static no.nav.foreldrepenger.behandlingslager.testutilities.behandling.BehandlingslagerTestUtil.lagNavBruker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.BehandlendeFagsystem;
import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.VurderFagsystem;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Arbeidsforhold;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.Organisasjon;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.util.FPDateUtil;

public class VurderFagsystemTjenesteImplTest {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("1");
    private static final Long ÅPEN_FAGSAK_ID_1 = 1L;
    private static final Long ÅPEN_FAGSAK_ID_2 = 2L;
    private static final Saksnummer ÅPEN_SAKSNUMMER_1 = new Saksnummer(ÅPEN_FAGSAK_ID_1 * 2 + "");
    private static final Saksnummer ÅPEN_SAKSNUMMER_2 = new Saksnummer(ÅPEN_FAGSAK_ID_2 * 2 + "");
    private static final Long AVSLT_NY_FAGSAK_ID_1 = 11L;
    private static final Long AVSLT_NY_FAGSAK_ID_2 = 2L;
    private static final Long AVSLT_GAMMEL_FAGSAK_ID_1 = 111L;
    private static final AktørId ANNEN_PART_ID = new AktørId("28");
    private static final AktørId BRUKER_AKTØR_ID = new AktørId("123");
    private static final LocalDate BARN_TERMINDATO = LocalDate.of(2019, 02, 03);
    private static final LocalDate BARN_FØDSELSDATO = LocalDate.of(2019, 02, 04);
    private static final String ARBEIDSFORHOLDSID = "arbeidsforholdsId123";
    private static final String VIRKSOMHETSNUMMER = "123456789";
    public static final Period VENTE_FRIST_AAREG = Period.parse("P1D");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private VurderFagsystemTjenesteImpl vurderFagsystemTjeneste;
    @Mock
    private BehandlingRepository behandlingRepositoryMock;
    private FamilieHendelseRepository grunnlagRepository;

    @Mock
    private FagsakRepository fagsakRepositoryMock;
    @Mock
    private FagsakTjeneste fagsakTjenesteMock;
    @Mock
    private ArbeidsforholdTjeneste arbeidsForholdTjenesteMock;
    @Mock
    private TpsTjeneste tpsTjenesteMock;

    private Fagsak fagsakFødselES = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, lagNavBruker(), null, ÅPEN_SAKSNUMMER_1);
    private Fagsak fagsakAdopsjonES = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, null);
    private Fagsak fagsakFødselFP = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, lagNavBruker(), null, ÅPEN_SAKSNUMMER_2);
    private Fagsak fagsakAnnenPartFP = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, null);
    private Fagsak fpFagsakUdefinert = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, lagNavBruker());

    private Fagsak fagsakSpyFP = spy(Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, lagNavBruker()));

    @Before
    public void setUp() {
        BehandlingRepositoryProvider repositoryProvider = mock(BehandlingRepositoryProvider.class);
        behandlingRepositoryMock = mock(BehandlingRepository.class);
        when(repositoryProvider.getBehandlingRepository()).thenReturn(behandlingRepositoryMock);
        grunnlagRepository = mock(FamilieHendelseRepository.class);
        when(repositoryProvider.getFamilieGrunnlagRepository()).thenReturn(grunnlagRepository);
        fagsakRepositoryMock = mock(FagsakRepository.class);
        when(repositoryProvider.getFagsakRepository()).thenReturn(fagsakRepositoryMock);
        fagsakTjenesteMock = mock(FagsakTjeneste.class);
        arbeidsForholdTjenesteMock = Mockito.mock(ArbeidsforholdTjeneste.class);
        tpsTjenesteMock = Mockito.mock(TpsTjeneste.class);
        MottatteDokumentTjeneste mottatteDokumentTjenesteMock = Mockito.mock(MottatteDokumentTjeneste.class);
        vurderFagsystemTjeneste = new VurderFagsystemTjenesteImpl(VENTE_FRIST_AAREG, fagsakTjenesteMock, arbeidsForholdTjenesteMock, tpsTjenesteMock, repositoryProvider, mottatteDokumentTjenesteMock);

        fagsakFødselES.setId(1L);
        fagsakAdopsjonES.setId(2L);
    }

    @Test
    public void nesteStegSkalVæreManuellVurderingHvisBrukerIkkeHarSakIVlForUstrukturertDokument() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setDokumentTypeId(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(Collections.emptyList());

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    @Test
    public void nesteStegSkalVæreInfotrygdHvisSakErFlaggetSkalBehandlesAvInfotrygd() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, lagNavBruker());
        fagsak.setSkalTilInfotrygd(true);
        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(fagsak);

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
    }

    @Test
    public void ustrukturertForsendelseSkalKnyttesTilBrukersNyesteÅpneSakHvisSlikFinnesOgJournalføres() {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(ÅPEN_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(ÅPEN_FAGSAK_ID_1), 10));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(ÅPEN_FAGSAK_ID_2, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(ÅPEN_FAGSAK_ID_2), 12));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_NY_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_NY_FAGSAK_ID_1), 2));

        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO.minusDays(340), BARN_FØDSELSDATO.minusDays(340));
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));
        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(buildFagsak(ÅPEN_FAGSAK_ID_1, false, FagsakYtelseType.ENGANGSTØNAD));
        saksliste.add(buildFagsak(ÅPEN_FAGSAK_ID_2, false, FagsakYtelseType.ENGANGSTØNAD));
        saksliste.add(buildFagsak(AVSLT_NY_FAGSAK_ID_1, true, FagsakYtelseType.ENGANGSTØNAD));

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(result.getSaksnummer()).hasValueSatisfying(it -> assertThat(it).isEqualTo(ÅPEN_SAKSNUMMER_1));

    }

    @Test
    public void ustrukturertForsendelseSkalKnyttesTilBrukersNyesteÅpneSakHvisUspesifikk() {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(ÅPEN_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(Optional.empty());
        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(buildFagsak(ÅPEN_FAGSAK_ID_1, false, FagsakYtelseType.ENGANGSTØNAD));

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(result.getSaksnummer()).hasValueSatisfying(it -> assertThat(it).isEqualTo(ÅPEN_SAKSNUMMER_1));
    }

    private Fagsak fagsakFødselMedId(Long forventetFagsakId) {

        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.ENGANGSTØNAD, lagNavBruker());
        fagsak.setId(forventetFagsakId);
        return fagsak;
    }

    @Test
    public void ustrukturertForsendelseSkalSendesTilManuellBehandlingHvisNyesteAvsluttedeSakErNyereEnn3mndOgÅpenSakIkkeFinnes() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_NY_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_NY_FAGSAK_ID_1), 40));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_NY_FAGSAK_ID_2, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_NY_FAGSAK_ID_2), 52));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_GAMMEL_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_GAMMEL_FAGSAK_ID_1), 200));

        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO.minusDays(340), BARN_FØDSELSDATO.minusDays(340));
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));
        List<Fagsak> saksliste = new ArrayList<>();

        saksliste.add(buildFagsak(AVSLT_NY_FAGSAK_ID_1, true, FagsakYtelseType.ENGANGSTØNAD));
        saksliste.add(buildFagsak(AVSLT_NY_FAGSAK_ID_2, true, FagsakYtelseType.ENGANGSTØNAD));
        saksliste.add(buildFagsak(AVSLT_GAMMEL_FAGSAK_ID_1, true, FagsakYtelseType.ENGANGSTØNAD));

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);

    }

    @Test
    public void ustrukturertVedleggSkalSendesTilManuellBehandlingHvisIngenSaker() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any()))
            .thenReturn(Optional.empty());

        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.empty());
        List<Fagsak> saksliste = new ArrayList<>();

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    @Test
    public void ustrukturertSøknadSkalSendesTilManuellBehandlingHvisIngenSaker() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any()))
            .thenReturn(Optional.empty());

        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.empty());
        List<Fagsak> saksliste = new ArrayList<>();

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
    }

    @Test
    public void ustrukturertForsendelseSkalSendesTilManuellBehandlingHvisÅpenSakIkkeFinnesOgAvsluttedeSakerErEldreEnn3mndOgNyereEnn10Mnd() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_NY_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_NY_FAGSAK_ID_1), 140));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_GAMMEL_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_GAMMEL_FAGSAK_ID_1), 452));

        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO.minusDays(340), BARN_FØDSELSDATO.minusDays(340));
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));
        List<Fagsak> saksliste = new ArrayList<>();

        saksliste.add(buildFagsak(AVSLT_NY_FAGSAK_ID_1, true, FagsakYtelseType.ENGANGSTØNAD));
        saksliste.add(buildFagsak(AVSLT_GAMMEL_FAGSAK_ID_1, true, FagsakYtelseType.ENGANGSTØNAD));

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    @Test
    public void nyVLSakSkalOpprettesForUstrukturertSøknadDersomBrukerIkkerHarÅpenSakNyesteAvsluttedeSakErEldreEnn10mnd() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_GAMMEL_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_GAMMEL_FAGSAK_ID_1), 340));

        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO.minusDays(340), BARN_FØDSELSDATO.minusDays(340));
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));
        List<Fagsak> saksListe = new ArrayList<>();

        saksListe.add(buildFagsak(AVSLT_GAMMEL_FAGSAK_ID_1, true, FagsakYtelseType.ENGANGSTØNAD));

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksListe);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
    }

    @Test
    public void nesteStegSkalVæreInfotrygdDersomEksisterendeSakerGjelderAnnetBehandlingTema() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(false);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(AVSLT_GAMMEL_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(AVSLT_GAMMEL_FAGSAK_ID_1), 340));

        List<Fagsak> saksListe = new ArrayList<>();

        saksListe.add(buildFagsak(AVSLT_GAMMEL_FAGSAK_ID_1, true, FagsakYtelseType.FORELDREPENGER));
        saksListe.add(buildFagsak(ÅPEN_FAGSAK_ID_1, false, FagsakYtelseType.FORELDREPENGER));

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksListe);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
    }

    private BehandlendeFagsystem toVurderFagsystem(VurderFagsystem vfData) {
        return vurderFagsystemTjeneste.vurderFagsystem(vfData);

    }

    private Optional<Behandling> byggBehandlingMedEndretDato(Fagsak fagsak, int dagerSidenSisteBehandling) {

        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak)
            .medOpprettetDato(LocalDateTime.now().minusDays(dagerSidenSisteBehandling));
        Behandling behandling = behandlingBuilder.build();
        return Optional.of(behandling);
    }

    @Test
    public void nesteStegSkalVæreHentOgVurderInfotrygdHvisPassendeSakIkkeFinnesForStukturertDokument() throws Exception {
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
    }

    @Test
    public void nesteStegSkalVæreOpprettGSakOppgaveHvisMerEnnEnSakPasserForStukturertDokument() throws Exception {
        LocalDate terminDatdato = LocalDate.of(2017, 7, 1);

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setBarnTermindato(terminDatdato);

        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(fagsakFødselES);
        saksliste.add(fagsakFødselES);

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakFødselES));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(terminDatdato, null);
        when(grunnlagRepository.hentAggregat(behandling.get())).thenReturn(grunnlag);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    @Test
    public void nesteStegSkalVæreTilJournalføringeHvisAkkurattEnÅpenSakPasserForStukturertDokument() throws Exception {
        LocalDate terminDatdato = LocalDate.of(2017, 7, 1);

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setBarnTermindato(terminDatdato);

        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(fagsakFødselES);
        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakFødselES));
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(terminDatdato, null);
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);

        when(grunnlagRepository.hentAggregat(behandling.get())).thenReturn(grunnlag);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
    }

    @Test
    public void nesteStegSkalVæreTilJournalføringeHvisAkkurattEnÅpenSakUtenBehandlingPasserForStukturertDokument() throws Exception {
        LocalDate terminDatdato = LocalDate.of(2017, 7, 1);

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setBarnTermindato(terminDatdato);

        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(fagsakFødselES);
        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(Optional.empty());

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
    }


    @Test
    public void nesteStegSkalVæreTilJournalføringeHvisEnSakMedLukketBehandlingSakPasserForStukturertDokument() throws Exception {
        LocalDate terminDatdato = LocalDate.of(2017, 7, 1);

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setBarnTermindato(terminDatdato);

        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(fagsakFødselES);
        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakFødselES));
        behandling.get().avsluttBehandling();
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(terminDatdato, null);
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        when(grunnlagRepository.hentAggregat(behandling.get())).thenReturn(grunnlag);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    @Test
    public void nesteStegSkalVæreManuellBehandlingHvisIngenSakPasserMenDetFinnesEnAvsluttetSak() {
        LocalDate terminDatdato = LocalDate.of(2017, 7, 1);

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setBarnTermindato(terminDatdato);

        List<Fagsak> saksliste = new ArrayList<>();
        Fagsak fagsak = buildFagsak(AVSLT_NY_FAGSAK_ID_1, true, FagsakYtelseType.FORELDREPENGER);
        Behandling behandling = byggBehandlingFødsel(fagsak);
        behandling.avsluttBehandling();
        Optional<Behandling> behandlingOpt = Optional.of(behandling);
        saksliste.add(fagsak);
        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(terminDatdato, null);
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandlingOpt);
        when(grunnlagRepository.hentAggregat(behandlingOpt.get())).thenReturn(grunnlag);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandlingOpt.get())).thenReturn(Optional.of(grunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getSaksnummer()).isEmpty();
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    @Test
    public void nesteStegSkalVæreTilJournalføringeHvisJournalpostAlleredeLagretPåFagsaken() throws Exception {
        LocalDate terminDatdato = LocalDate.of(2017, 7, 1);

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);
        vfData.setBarnTermindato(terminDatdato);

        Journalpost journalpost = new Journalpost(JOURNALPOST_ID, fagsakFødselES);
        when(fagsakTjenesteMock.hentJournalpost(any())).thenReturn(Optional.of(journalpost));

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
    }

    @Test
    public void nesteStegSkalVæreHentÅVurderInfotrygdSakHvisIngenSakPasserForStukturertDokument() throws Exception {

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);

        List<Fagsak> saksliste = new ArrayList<>();
        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        when(fagsakTjenesteMock.hentJournalpost(any())).thenReturn(Optional.empty());

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
    }

    @Test
    public void nesteStegSkalVæreHentOgVurderInfotrygdSakHvisBrukerHarSakIVLMenDenIkkePasserForStukturertDokument() throws Exception {

        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setJournalpostId(JOURNALPOST_ID);

        List<Fagsak> fagsakListe = new ArrayList<>();
        fagsakListe.add(fagsakFødselES);
        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(fagsakListe);

        when(fagsakTjenesteMock.hentJournalpost(any())).thenReturn(Optional.empty());

        LocalDate terminDatDato = LocalDate.of(2018, 7, 1);

        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakFødselES));
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(terminDatDato, null);
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(vfData);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
    }

    private Behandling byggBehandlingUdefinert(Fagsak fagsak) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
        return behandlingBuilder.build();
    }

    private VurderFagsystem byggVurderFagsystem(String årsakInnsending, BehandlingTema behandlingTema, LocalDateTime forsendelseMottatt) {
        VurderFagsystem fagsystem = new VurderFagsystem();
        fagsystem.setAktørId(new AktørId("123"));
        fagsystem.setJournalpostId(JOURNALPOST_ID);
        fagsystem.setBehandlingTema(behandlingTema);
        fagsystem.setÅrsakInnsendingInntektsmelding(årsakInnsending);
        fagsystem.setForsendelseMottattTidspunkt(forsendelseMottatt);
        fagsystem.setArbeidsforholdsid(ARBEIDSFORHOLDSID);
        fagsystem.setVirksomhetsnummer(VIRKSOMHETSNUMMER);
        return fagsystem;
    }

    @Test
    public void skalReturnereTrueNårFagÅrsakTypeErUdefinertOgBehandlingTemaErForeldrePenger() {
        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        assertThat(fpFagsakUdefinert.getYtelseType().gjelderForeldrepenger() && fpFagsakUdefinert.erÅpen()).isTrue();
    }

    @Test
    public void skalReturnereTrueNårFagÅrsakTypeErUdefinertOgBehandlingTemaErForeldrePengerFødsel() {
        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        assertThat(fpFagsakUdefinert.getYtelseType().gjelderForeldrepenger() && fpFagsakUdefinert.erÅpen()).isTrue();
    }

    @Test
    public void skalReturnereInfotrygdBehandlingNårIngenÅpneSakerFinnesPåBrukerForEngangsstønad() {
        VurderFagsystem fagsystem = byggVurderFagsystem(VurderFagsystem.ÅRSAK_NY, BehandlingTema.ENGANGSSTØNAD, LocalDateTime.now(FPDateUtil.getOffset()));
        fagsystem.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(Collections.emptyList());

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
    }

    private Fagsak buildFagsakMedUdefinertRelasjon(Long fagsakid, boolean erAvsluttet) {
        NavBruker navBruker = lagNavBruker();
        Fagsak fagsak = FagsakBuilder.nyForeldrepengesak(RelasjonsRolleType.MORA)
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer(fagsakid + ""))
            .build();
        fagsak.setId(fagsakid);
        if (erAvsluttet) {
            fagsak.setAvsluttet();
        }
        return fagsak;
    }

    @Test
    public void skalReturnereManuellBehandlingNårFlereÅpneSakerFinnesPåBruker() {
        VurderFagsystem fagsystem = byggVurderFagsystem(VurderFagsystem.ÅRSAK_NY, BehandlingTema.FORELDREPENGER, LocalDateTime.now(FPDateUtil.getOffset()));

        when(fagsakRepositoryMock.hentJournalpost(any())).thenReturn(Optional.empty());

        List<Fagsak> saksliste = new ArrayList<>();
        saksliste.add(buildFagsakMedUdefinertRelasjon(ÅPEN_FAGSAK_ID_1, false));
        saksliste.add(buildFagsakMedUdefinertRelasjon(ÅPEN_FAGSAK_ID_2, false));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(ÅPEN_FAGSAK_ID_1, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(ÅPEN_FAGSAK_ID_1), 10));

        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(ÅPEN_FAGSAK_ID_2, asList(KLAGE, INNSYN)))
            .thenReturn(byggBehandlingMedEndretDato(fagsakFødselMedId(ÅPEN_FAGSAK_ID_2), 12));
        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(saksliste);

        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));
        leggInnArbeidsforholdIAareg();

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    @Test
    public void skalReturnereVedtaksløsningMedSaksnummerNårEnSakFinnesOgÅrsakInnsendingErEndring() {
        VurderFagsystem fagsystem = byggVurderFagsystem(VurderFagsystem.ÅRSAK_ENDRING, BehandlingTema.FORELDREPENGER, LocalDateTime.now(FPDateUtil.getOffset()));

        when(fagsakRepositoryMock.hentJournalpost(any())).thenReturn(Optional.empty());

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(Collections.singletonList(buildFagsakMedUdefinertRelasjon(123L, false)));

        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        leggInnArbeidsforholdIAareg();

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(result.getSaksnummer()).isNotEmpty();
    }

    @Test
    public void skalReturnereVedtaksløsningMedSaksnummerNårEnSakFinnesMedStatusOpprett() {
        VurderFagsystem fagsystem = byggVurderFagsystem(VurderFagsystem.ÅRSAK_NY, BehandlingTema.FORELDREPENGER, LocalDateTime.now(FPDateUtil.getOffset()));

        when(fagsakRepositoryMock.hentJournalpost(any())).thenReturn(Optional.empty());

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(Collections.singletonList(buildFagsakMedUdefinertRelasjon(123L, false)));

        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));
        leggInnArbeidsforholdIAareg();

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(result.getSaksnummer()).isNotEmpty();
    }

    @Test
    public void skalReturnereVedtaksløsningMedSaksnummerNårEnSakFinnesMedStatusLøpende() {
        VurderFagsystem fagsystem = byggVurderFagsystem(VurderFagsystem.ÅRSAK_NY, BehandlingTema.FORELDREPENGER, LocalDateTime.now(FPDateUtil.getOffset()));

        when(fagsakRepositoryMock.hentJournalpost(any())).thenReturn(Optional.empty());

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(Collections.singletonList(buildFagsakMedUdefinertRelasjon(123L, false)));
        when(fagsakSpyFP.getStatus()).thenReturn(FagsakStatus.LØPENDE);

        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(any(), any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));
        leggInnArbeidsforholdIAareg();

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(result.getSaksnummer()).isNotEmpty();
    }

    @Test
    public void skalReturnereVLMedSaksnummerNårSaksnummerFraSøknadFinnesIVL() {
        VurderFagsystem fagsystem = byggVurderFagsystemMedAnnenPart(BehandlingTema.FORELDREPENGER_FØDSEL, ANNEN_PART_ID, ÅPEN_SAKSNUMMER_1);

        when(fagsakRepositoryMock.hentSakGittSaksnummer(ÅPEN_SAKSNUMMER_1)).thenReturn(Optional.of(fagsakFødselFP));

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(result.getSaksnummer()).hasValue(ÅPEN_SAKSNUMMER_1);
    }

    @Test
    public void skalReturnereManuellBehandlingNårSaksnummrFraSøknadIkkeFinnesIVLOgAnnenPartIkkeHarSakForSammeBarnIVL() {
        VurderFagsystem fagsystem = byggVurderFagsystemMedAnnenPart(BehandlingTema.FORELDREPENGER_FØDSEL, ANNEN_PART_ID, ÅPEN_SAKSNUMMER_1);

        when(fagsakRepositoryMock.hentSakGittSaksnummer(ÅPEN_SAKSNUMMER_1)).thenReturn(Optional.empty());
        when(fagsakRepositoryMock.hentForBrukerAktørId(ANNEN_PART_ID)).thenReturn(Collections.singletonList(fagsakAnnenPartFP));

        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakFødselFP));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(fagsakFødselFP.getId(), asList(KLAGE, INNSYN))).thenReturn(behandling);

        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO.minusDays(10), BARN_FØDSELSDATO.minusDays(10));
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
        assertThat(result.getSaksnummer()).isEmpty();
    }

    @Test
    public void skalKasteTekniskFeilNårSaksnummerFraSøknadIkkeFinnesIVLOgAnnenPartHarSakForSammeBarnIVL() {

        VurderFagsystem fagsystem = byggVurderFagsystemMedAnnenPart(BehandlingTema.FORELDREPENGER_FØDSEL, ANNEN_PART_ID, ÅPEN_SAKSNUMMER_1);

        when(fagsakRepositoryMock.hentSakGittSaksnummer(ÅPEN_SAKSNUMMER_1)).thenReturn(Optional.empty());
        when(fagsakRepositoryMock.hentForBruker(ANNEN_PART_ID)).thenReturn(Collections.singletonList(fagsakAnnenPartFP));

        long annenPartSakId = 222L;
        fagsakAnnenPartFP.setId(annenPartSakId);
        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakAnnenPartFP));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(annenPartSakId, asList(KLAGE, INNSYN))).thenReturn(behandling);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO, BARN_FØDSELSDATO);
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));

        expectedException
            .expectMessage("Kan ikke finne " + ÅPEN_SAKSNUMMER_1 + " fra søknad i Vedtaksløsningen selv om annen part har sak i VL for samme barn");
        toVurderFagsystem(fagsystem);
    }

    @Test
    public void skalReturnereVLNårSøknadIkkeHarSaksnummmerOgAnnenPartHarSakForSammeBarnIVL() {
        VurderFagsystem fagsystem = byggVurderFagsystemMedAnnenPart(BehandlingTema.FORELDREPENGER_FØDSEL, ANNEN_PART_ID, null);

        when(fagsakRepositoryMock.hentSakGittSaksnummer(ÅPEN_SAKSNUMMER_1)).thenReturn(Optional.empty());
        when(fagsakRepositoryMock.hentForBruker(ANNEN_PART_ID)).thenReturn(Collections.singletonList(fagsakAnnenPartFP));

        long annenPartSakId = 222L;
        fagsakAnnenPartFP.setId(annenPartSakId);
        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakAnnenPartFP));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(annenPartSakId, asList(KLAGE, INNSYN))).thenReturn(behandling);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO, BARN_FØDSELSDATO);
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(result.getSaksnummer()).isEmpty();
    }

    @Test
    public void skalReturnereInfotrygdNårSøknadIkkeHarSaksnummerOgAnnenPartIkkeHarSakForSammeBarnIVL() {
        VurderFagsystem fagsystem = byggVurderFagsystemMedAnnenPart(BehandlingTema.FORELDREPENGER_FØDSEL, ANNEN_PART_ID, null);

        when(fagsakRepositoryMock.hentSakGittSaksnummer(ÅPEN_SAKSNUMMER_1)).thenReturn(Optional.empty());
        when(fagsakRepositoryMock.hentForBruker(ANNEN_PART_ID)).thenReturn(Collections.singletonList(fagsakAnnenPartFP));

        long annenPartSakId = 222L;
        fagsakAnnenPartFP.setId(annenPartSakId);
        Optional<Behandling> behandling = Optional.of(byggBehandlingFødsel(fagsakAnnenPartFP));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(annenPartSakId, asList(KLAGE, INNSYN))).thenReturn(behandling);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = byggFødselGrunnlag(BARN_TERMINDATO.minusDays(10), BARN_FØDSELSDATO.minusDays(10));
        when(grunnlagRepository.hentAggregatHvisEksisterer(any(Behandling.class))).thenReturn(Optional.of(familieHendelseGrunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.INFOTRYGD);
        assertThat(result.getSaksnummer()).isEmpty();

    }

    @Test
    public void skalReturnerePrøvIgjenNårErInntektsmeldingOgArbeidsforholdIkkeFinnesIAareg() {
        VurderFagsystem fagsystem = byggVurderFagsystem(VurderFagsystem.ÅRSAK_NY, BehandlingTema.FORELDREPENGER, LocalDateTime.now(FPDateUtil.getOffset()));

        when(fagsakRepositoryMock.hentJournalpost(any())).thenReturn(Optional.empty());

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(Collections.singletonList(buildFagsakMedUdefinertRelasjon(123L, false)));

        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakId(any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.PRØV_IGJEN);
    }

    @Test
    public void skalReturnereManuellVurderingNårErInntektsmeldingOgArbeidsforholdIkkeFinnesIAaregOgFristHarPassert() {
        VurderFagsystem fagsystem = byggVurderFagsystem(VurderFagsystem.ÅRSAK_NY, BehandlingTema.FORELDREPENGER, LocalDateTime.now(FPDateUtil.getOffset()).minusDays(1));

        when(fagsakRepositoryMock.hentJournalpost(any())).thenReturn(Optional.empty());

        when(fagsakRepositoryMock.hentForBruker(any())).thenReturn(Collections.singletonList(buildFagsakMedUdefinertRelasjon(123L, false)));

        Optional<Behandling> behandling = Optional.of(byggBehandlingUdefinert(fpFagsakUdefinert));
        when(behandlingRepositoryMock.hentSisteBehandlingForFagsakId(any())).thenReturn(behandling);
        final FamilieHendelseGrunnlag grunnlag = byggFødselGrunnlag(null, null);
        when(grunnlagRepository.hentAggregatHvisEksisterer(behandling.get())).thenReturn(Optional.of(grunnlag));

        BehandlendeFagsystem result = toVurderFagsystem(fagsystem);
        assertThat(result.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
    }

    private VurderFagsystem byggVurderFagsystemMedAnnenPart(BehandlingTema behandlingTema, AktørId annenPartId, Saksnummer saksnr) {
        VurderFagsystem fagsystem = new VurderFagsystem();
        fagsystem.setAnnenPart(annenPartId);
        fagsystem.setSaksnummer(saksnr);
        fagsystem.setStrukturertSøknad(true);
        fagsystem.setAktørId(BRUKER_AKTØR_ID);
        fagsystem.setJournalpostId(JOURNALPOST_ID);
        fagsystem.setBehandlingTema(behandlingTema);
        fagsystem.setBarnTermindato(BARN_TERMINDATO);
        fagsystem.setBarnFodselsdato(BARN_FØDSELSDATO);
        return fagsystem;
    }

    private void leggInnArbeidsforholdIAareg() {
        when(tpsTjenesteMock.hentFnrForAktør(any(AktørId.class))).thenReturn(new PersonIdent(BRUKER_AKTØR_ID.getId()));

        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholdMap = new HashMap<>();
        List<Arbeidsforhold> arbeidsforholdListe = new ArrayList<>();
        String arbeidsforholdsId = ARBEIDSFORHOLDSID;
        Organisasjon arbeidsgiver = new Organisasjon(VIRKSOMHETSNUMMER);
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold.Builder()
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdId(arbeidsforholdsId)
            .build();
        arbeidsforholdListe.add(arbeidsforhold);
        ArbeidsforholdIdentifikator arbeidsforholdsIdentifikator = new ArbeidsforholdIdentifikator(arbeidsgiver, ArbeidsforholdRef.ref(arbeidsforholdsId), "type");
        arbeidsforholdMap.put(arbeidsforholdsIdentifikator, arbeidsforholdListe);
        when(arbeidsForholdTjenesteMock.finnArbeidsforholdForIdentIPerioden(any(PersonIdent.class), any(Interval.class))).thenReturn(arbeidsforholdMap);
    }

}
