package no.nav.foreldrepenger.domene.dokumentarkiv.impl;

import static no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil.convertToXMLGregorianCalendar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.Kommunikasjonsretning;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.DokumenttypeIder;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journalposttyper;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.DetaljertDokumentinformasjon;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.DokumentInnhold;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import no.nav.vedtak.felles.integrasjon.journal.v3.JournalConsumer;

public class DokumentArkivTjenesteImplTest {

    private static final JournalpostId JOURNAL_ID = new JournalpostId("42");
    private static final String DOKUMENT_ID = "66";
    private static final Saksnummer KJENT_SAK = new Saksnummer("123456");
    private static final String DOKUMENT_TITTEL_TERMINBEKREFTELSE = "Terminbekreftelse";
    private static final LocalDate NOW = LocalDate.now();
    private static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

    private DokumentArkivTjenesteImpl dokumentApplikasjonTjeneste;
    private JournalConsumer mockJournalProxyService;

    private KodeverkRepository kodeverkRepository;


    @Before
    public void setUp() {
        mockJournalProxyService = mock(JournalConsumer.class);
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        final FagsakRepository fagsakRepository = mock(FagsakRepository.class);
        final Fagsak fagsak = mock(Fagsak.class);
        final Optional<Fagsak> mock1 = Optional.of(fagsak);
        when(fagsakRepository.hentSakGittSaksnummer(any(Saksnummer.class))).thenReturn(mock1);
        kodeverkRepository.finn(VariantFormat.class, VariantFormat.ORIGINAL);
        kodeverkRepository.finn(VariantFormat.class, VariantFormat.ARKIV);
        kodeverkRepository.finn(ArkivFilType.class, ArkivFilType.PDF);
        kodeverkRepository.finn(ArkivFilType.class, ArkivFilType.PDFA);
        kodeverkRepository.finn(ArkivFilType.class, ArkivFilType.XML);
        dokumentApplikasjonTjeneste = new DokumentArkivTjenesteImpl(mockJournalProxyService, kodeverkRepository, fagsakRepository);
    }

    @Test
    public void skalRetunereDokumentListeMedJournalpostTypeInn() throws Exception {
        HentKjerneJournalpostListeResponse hentJournalpostListeResponse = new HentKjerneJournalpostListeResponse();
        hentJournalpostListeResponse.getJournalpostListe().add(
            createJournalpost(ArkivFilType.PDF, VariantFormat.ARKIV, YESTERDAY, NOW,  "U"));
        when(mockJournalProxyService.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(hentJournalpostListeResponse);

        List<ArkivJournalPost> arkivDokuments = dokumentApplikasjonTjeneste.hentAlleDokumenterForVisning(KJENT_SAK);

        assertThat(arkivDokuments).isNotEmpty();
        ArkivJournalPost arkivJournalPost = arkivDokuments.get(0);
        ArkivDokument arkivDokument = arkivJournalPost.getHovedDokument();
        assertThat(arkivJournalPost.getJournalpostId()).isEqualTo(JOURNAL_ID);
        assertThat(arkivDokument.getDokumentId()).isEqualTo(DOKUMENT_ID);
        assertThat(arkivDokument.getTittel()).isEqualTo(DOKUMENT_TITTEL_TERMINBEKREFTELSE);
        assertThat(arkivJournalPost.getTidspunkt()).isEqualTo(YESTERDAY);
        assertThat(arkivJournalPost.getKommunikasjonsretning()).isEqualTo(Kommunikasjonsretning.UT);
    }

    @Test
    public void skalRetunereDokumentListeMedJournalpostTypeUt() throws Exception {
        HentKjerneJournalpostListeResponse hentJournalpostListeResponse = new HentKjerneJournalpostListeResponse();
        hentJournalpostListeResponse.getJournalpostListe().add(
            createJournalpost(ArkivFilType.PDFA, VariantFormat.ARKIV, YESTERDAY, NOW,"I"));
        when(mockJournalProxyService.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(hentJournalpostListeResponse);

        List<ArkivJournalPost> arkivDokuments = dokumentApplikasjonTjeneste.hentAlleDokumenterForVisning(KJENT_SAK);

        assertThat(arkivDokuments.get(0).getTidspunkt()).isEqualTo(YESTERDAY);
        assertThat(arkivDokuments.get(0).getKommunikasjonsretning()).isEqualTo(Kommunikasjonsretning.INN);
    }

    @Test
    public void skalRetunereDokumentListeMedUansettInnhold() throws Exception {
        HentKjerneJournalpostListeResponse hentJournalpostListeResponse = new HentKjerneJournalpostListeResponse();
        hentJournalpostListeResponse.getJournalpostListe().addAll(Arrays.asList(
            createJournalpost(ArkivFilType.PDFA, VariantFormat.ARKIV, YESTERDAY, NOW,"I"),
            createJournalpost(ArkivFilType.XLS, VariantFormat.ARKIV)));
        when(mockJournalProxyService.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(hentJournalpostListeResponse);

        Optional<ArkivJournalPost> arkivDokument = dokumentApplikasjonTjeneste.hentJournalpostForSak(KJENT_SAK, JOURNAL_ID);

        assertThat(arkivDokument).isPresent();
        assertThat(arkivDokument.get().getAndreDokument()).hasSize(0);
    }

    @Test
    public void skalRetunereDokumenterAvFiltypePDF() throws Exception {
        HentKjerneJournalpostListeResponse hentJournalpostListeResponse = new HentKjerneJournalpostListeResponse();
        hentJournalpostListeResponse.getJournalpostListe().addAll(Arrays.asList(createJournalpost(ArkivFilType.XML, VariantFormat.ARKIV),
            createJournalpost(ArkivFilType.PDF, VariantFormat.ARKIV),
            createJournalpost(ArkivFilType.XML, VariantFormat.ARKIV)));
        when(mockJournalProxyService.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(hentJournalpostListeResponse);

        List<ArkivJournalPost> arkivDokuments = dokumentApplikasjonTjeneste.hentAlleDokumenterForVisning(KJENT_SAK);

        assertThat(arkivDokuments).isNotEmpty();
        assertThat(arkivDokuments.size()).isEqualTo(1);
    }

    @Test
    public void skalRetunereDokumenterAvVariantFormatARKIV() throws Exception {
        HentKjerneJournalpostListeResponse hentJournalpostListeResponse = new HentKjerneJournalpostListeResponse();
        hentJournalpostListeResponse.getJournalpostListe().addAll(Arrays.asList(createJournalpost(ArkivFilType.XML, VariantFormat.ORIGINAL),
            createJournalpost(ArkivFilType.PDF, VariantFormat.ARKIV),
            createJournalpost(ArkivFilType.PDFA, VariantFormat.ARKIV),
            createJournalpost(ArkivFilType.XML, VariantFormat.ORIGINAL)));
        when(mockJournalProxyService.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(hentJournalpostListeResponse);

        List<ArkivJournalPost> arkivDokuments = dokumentApplikasjonTjeneste.hentAlleDokumenterForVisning(KJENT_SAK);

        assertThat(arkivDokuments).isNotEmpty();
        assertThat(arkivDokuments.size()).isEqualTo(2);
    }

    @Test
    public void skalRetunereDokumentListeMedSisteTidspunktØverst() throws Exception {
        HentKjerneJournalpostListeResponse hentJournalpostListeResponse = new HentKjerneJournalpostListeResponse();
        hentJournalpostListeResponse.getJournalpostListe().addAll(Arrays.asList(
            createJournalpost(ArkivFilType.PDFA, VariantFormat.ARKIV, NOW, NOW, "U"),
            createJournalpost(ArkivFilType.PDFA, VariantFormat.ARKIV, YESTERDAY.minusDays(1), YESTERDAY, "I")));
        when(mockJournalProxyService.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(hentJournalpostListeResponse);

        List<ArkivJournalPost> arkivDokuments = dokumentApplikasjonTjeneste.hentAlleDokumenterForVisning(KJENT_SAK);

        assertThat(arkivDokuments.get(0).getTidspunkt()).isEqualTo(NOW);
        assertThat(arkivDokuments.get(0).getKommunikasjonsretning()).isEqualTo(Kommunikasjonsretning.UT);
        assertThat(arkivDokuments.get(1).getTidspunkt()).isEqualTo(YESTERDAY.minusDays(1));
        assertThat(arkivDokuments.get(1).getKommunikasjonsretning()).isEqualTo(Kommunikasjonsretning.INN);
    }

    @Test
    public void skal_kalle_web_service_og_oversette_fra_() throws HentDokumentDokumentIkkeFunnet, HentDokumentJournalpostIkkeFunnet, HentDokumentSikkerhetsbegrensning {
        // Arrange

        final byte[] bytesExpected = {1, 2, 7};
        HentDokumentResponse response = new HentDokumentResponse();
        response.setDokument(bytesExpected);
        when(mockJournalProxyService.hentDokument(any())).thenReturn(response);

        // Act

        byte[] bytesActual = dokumentApplikasjonTjeneste.hentDokumnet(new JournalpostId("123"), "456");

        // Assert
        assertThat(bytesActual).isEqualTo(bytesExpected);
    }

    @Test
    public void skalKorrigereManglendeDokTypeBasertPåTittel() throws Exception {
        DokumentTypeId fsf = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        final String tittel = "Søknad om foreldrepenger ved fødsel";
        HentKjerneJournalpostListeResponse hentJournalpostListeResponse = new HentKjerneJournalpostListeResponse();
        hentJournalpostListeResponse.getJournalpostListe().add(createJournalpost(ArkivFilType.PDFA, VariantFormat.ARKIV, YESTERDAY, NOW,"I"));
        hentJournalpostListeResponse.getJournalpostListe().get(0).getHoveddokument().setDokumentTypeId(null);
        hentJournalpostListeResponse.getJournalpostListe().get(0).getHoveddokument().setTittel(tittel);
        when(mockJournalProxyService.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(hentJournalpostListeResponse);

        KodeverkTestHelper.helperSetSvarForNavneOppslag(fsf);
        DokumentTypeId dtid = dokumentApplikasjonTjeneste.utledDokumentTypeFraTittel(KJENT_SAK, JOURNAL_ID);
        KodeverkTestHelper.helperSetSvarForNavneOppslag(null);

        assertThat(dtid).isEqualTo(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
    }


    private Journalpost createJournalpost(ArkivFilType arkivFilTypeKonst, VariantFormat variantFormatKonst) throws DatatypeConfigurationException {
        return createJournalpost(arkivFilTypeKonst, variantFormatKonst, NOW, NOW, "U");
    }

    private Journalpost createJournalpost(ArkivFilType arkivFilTypeKonst, VariantFormat variantFormatKonst, LocalDate sendt, LocalDate mottatt, String kommunikasjonsretning) throws DatatypeConfigurationException {
        Journalpost journalpost = new Journalpost();
        journalpost.setJournalpostId(JOURNAL_ID.getVerdi());
        ArkivFilType arkivFilType = kodeverkRepository.finn(ArkivFilType.class, arkivFilTypeKonst);
        VariantFormat variantFormat = kodeverkRepository.finn(VariantFormat.class, variantFormatKonst);
        journalpost.setHoveddokument(createDokumentinfoRelasjon(arkivFilType.getOffisiellKode(), variantFormat.getOffisiellKode()));
        Journalposttyper kommunikasjonsretninger = new Journalposttyper();
        kommunikasjonsretninger.setValue(kommunikasjonsretning);
        journalpost.setJournalposttype(kommunikasjonsretninger);
        journalpost.setForsendelseJournalfoert(convertToXMLGregorianCalendar(sendt));
        journalpost.setForsendelseMottatt(convertToXMLGregorianCalendar(mottatt));
        return journalpost;
    }

    private DetaljertDokumentinformasjon createDokumentinfoRelasjon(String filtype, String variantformat) {
        DetaljertDokumentinformasjon dokumentinfoRelasjon = new DetaljertDokumentinformasjon();
        dokumentinfoRelasjon.setDokumentId(DOKUMENT_ID);
        DokumenttypeIder dokumenttyper = new DokumenttypeIder();
        dokumenttyper.setValue(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getOffisiellKode());
        dokumentinfoRelasjon.setDokumentTypeId(dokumenttyper);
        Dokumentkategorier dokumentkategorier = new Dokumentkategorier();
        dokumentkategorier.setValue(DokumentKategori.SØKNAD.getOffisiellKode());
        dokumentinfoRelasjon.setDokumentkategori(dokumentkategorier);
        dokumentinfoRelasjon.setTittel(DOKUMENT_TITTEL_TERMINBEKREFTELSE);
        DokumentInnhold dokumentInnhold = new DokumentInnhold();
        Arkivfiltyper arkivfiltyper = new Arkivfiltyper();
        arkivfiltyper.setValue(filtype);
        dokumentInnhold.setArkivfiltype(arkivfiltyper);
        Variantformater variantformater = new Variantformater();
        variantformater.setValue(variantformat);
        dokumentInnhold.setVariantformat(variantformater);
        dokumentinfoRelasjon.getDokumentInnholdListe().add(dokumentInnhold);
        return dokumentinfoRelasjon;
    }
}
