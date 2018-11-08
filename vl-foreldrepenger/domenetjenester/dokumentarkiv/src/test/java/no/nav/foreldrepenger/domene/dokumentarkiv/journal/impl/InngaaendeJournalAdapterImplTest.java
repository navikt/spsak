package no.nav.foreldrepenger.domene.dokumentarkiv.journal.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalMetadata;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.modig.core.test.LogSniffer;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.ArkivSak;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinnhold;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.DokumenttypeIder;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Mottakskanaler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalConsumer;

public class InngaaendeJournalAdapterImplTest {

    private InngåendeJournalAdapterImpl adapter; // objektet vi tester

    private InngaaendeJournalConsumer mockConsumer;
    private KodeverkRepository kodeverkRepository;

    @Rule
    public LogSniffer logSniffer = new LogSniffer();

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("JP-ID");
    private MottakKanal mottakKanal;

    private ArkivFilType arkivFilTypeH;
    private VariantFormat variantFormatH;
    private DokumentKategori dokumentKategoriH;
    private DokumentTypeId dokumenttypeH;
    private static final String DOKUMENT_ID_H = "DOKID-H";

    private ArkivFilType arkivFilTypeV1;
    private VariantFormat variantFormatV1;
    private DokumentKategori dokumentKategoriV1;
    private DokumentTypeId dokumenttypeV1;
    private static final String DOKUMENT_ID_V1 = "DOKID-V1";

    private ArkivFilType arkivFilTypeV2;
    private VariantFormat variantFormatV2;
    private DokumentKategori dokumentKategoriV2;
    private DokumentTypeId dokumenttypeV2;
    private static final String DOKUMENT_ID_V2 = "DOKID-V2";

    @Before
    public void setup() {
        mockConsumer = mock(InngaaendeJournalConsumer.class);
        kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
        adapter = new InngåendeJournalAdapterImpl(mockConsumer, kodeverkRepository);

        mottakKanal = kodeverkRepository.finn(MottakKanal.class, MottakKanal.ALTINN);

        dokumenttypeH = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        dokumenttypeV1 = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
        dokumenttypeV2 = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.DOKUMENTASJON_AV_OMSORGSOVERTAKELSE);

        arkivFilTypeH = kodeverkRepository.finn(ArkivFilType.class, ArkivFilType.PDF);
        arkivFilTypeV1 = kodeverkRepository.finn(ArkivFilType.class, ArkivFilType.PDFA);
        arkivFilTypeV2 = kodeverkRepository.finn(ArkivFilType.class, ArkivFilType.PDF);

        dokumentKategoriH = kodeverkRepository.finn(DokumentKategori.class, DokumentKategori.ELEKTRONISK_SKJEMA);
        dokumentKategoriV1 = kodeverkRepository.finn(DokumentKategori.class, DokumentKategori.KLAGE_ELLER_ANKE);
        dokumentKategoriV2 = kodeverkRepository.finn(DokumentKategori.class, DokumentKategori.SØKNAD);

        variantFormatH = kodeverkRepository.finn(VariantFormat.class, VariantFormat.ARKIV);
        variantFormatV1 = kodeverkRepository.finn(VariantFormat.class, VariantFormat.BREVBESTILLING);
        variantFormatV2 = kodeverkRepository.finn(VariantFormat.class, VariantFormat.PRODUKSJON);
    }

    @Test
    public void test_hentMetadata_ok()
        throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
        HentJournalpostUgyldigInput, HentJournalpostSikkerhetsbegrensning {

        Dokumentinnhold dokinnholdHoved = lagDokumentinnhold(arkivFilTypeH, variantFormatH);
        Dokumentinformasjon dokinfoHoved = lagDokumentinformasjon(dokumentKategoriH, dokumenttypeH, DOKUMENT_ID_H, dokinnholdHoved);

        Dokumentinnhold dokinnholdVedlegg1 = lagDokumentinnhold(arkivFilTypeV1, variantFormatV1);
        Dokumentinformasjon dokinfoVedlegg1 = lagDokumentinformasjon(dokumentKategoriV1, dokumenttypeV1, DOKUMENT_ID_V1, dokinnholdVedlegg1);

        Dokumentinnhold dokinnholdVedlegg2 = lagDokumentinnhold(arkivFilTypeV2, variantFormatV2);
        Dokumentinformasjon dokinfoVedlegg2 = lagDokumentinformasjon(dokumentKategoriV2, dokumenttypeV2, DOKUMENT_ID_V2, dokinnholdVedlegg2);

        Mottakskanaler mottakskanal = new Mottakskanaler();
        mottakskanal.setValue(mottakKanal.getOffisiellKode());

        InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
        inngaaendeJournalpost.setMottakskanal(mottakskanal);
        inngaaendeJournalpost.setJournaltilstand(Journaltilstand.ENDELIG);
        inngaaendeJournalpost.setHoveddokument(dokinfoHoved);
        inngaaendeJournalpost.getVedleggListe().add(dokinfoVedlegg1);
        inngaaendeJournalpost.getVedleggListe().add(dokinfoVedlegg2);

        HentJournalpostResponse response = new HentJournalpostResponse();
        response.setInngaaendeJournalpost(inngaaendeJournalpost);

        when(mockConsumer.hentJournalpost(any(HentJournalpostRequest.class))).thenReturn(response);

        List<JournalMetadata<DokumentTypeId>> metadataList = adapter.hentMetadata(JOURNALPOST_ID);

        assertThat(metadataList.size()).isEqualTo(3);

        JournalMetadata<DokumentTypeId> metadataHoved = metadataList.get(0);
        assertThat(metadataHoved.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(metadataHoved.getDokumentId()).isEqualTo(DOKUMENT_ID_H);
        assertThat(metadataHoved.getArkivFilType()).isEqualTo(arkivFilTypeH);
        assertThat(metadataHoved.getDokumentKategori()).isEqualTo(dokumentKategoriH);
        assertThat(metadataHoved.getDokumentType()).isEqualTo(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        assertThat(metadataHoved.getMottakKanal()).isEqualTo(mottakKanal);
        assertThat(metadataHoved.getJournaltilstand()).isEqualTo(JournalMetadata.Journaltilstand.ENDELIG);
        assertThat(metadataHoved.getVariantFormat()).isEqualTo(variantFormatH);
        assertThat(metadataHoved.getErHoveddokument()).isTrue();

        JournalMetadata<DokumentTypeId> metadataVedlegg1 = metadataList.get(1);
        assertThat(metadataVedlegg1.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(metadataVedlegg1.getDokumentId()).isEqualTo(DOKUMENT_ID_V1);
        assertThat(metadataVedlegg1.getArkivFilType()).isEqualTo(arkivFilTypeV1);
        assertThat(metadataVedlegg1.getDokumentKategori()).isEqualTo(dokumentKategoriV1);
        assertThat(metadataVedlegg1.getDokumentType()).isEqualTo(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
        assertThat(metadataVedlegg1.getMottakKanal()).isEqualTo(mottakKanal);
        assertThat(metadataVedlegg1.getJournaltilstand()).isEqualTo(JournalMetadata.Journaltilstand.ENDELIG);
        assertThat(metadataVedlegg1.getVariantFormat()).isEqualTo(variantFormatV1);
        assertThat(metadataVedlegg1.getErHoveddokument()).isFalse();

        JournalMetadata<DokumentTypeId> metadataVedlegg2 = metadataList.get(2);
        assertThat(metadataVedlegg2.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(metadataVedlegg2.getDokumentId()).isEqualTo(DOKUMENT_ID_V2);
        assertThat(metadataVedlegg2.getArkivFilType()).isEqualTo(arkivFilTypeV2);
        assertThat(metadataVedlegg2.getDokumentKategori()).isEqualTo(dokumentKategoriV2);
        assertThat(metadataVedlegg2.getDokumentType()).isEqualTo(DokumentTypeId.DOKUMENTASJON_AV_OMSORGSOVERTAKELSE);
        assertThat(metadataVedlegg2.getMottakKanal()).isEqualTo(mottakKanal);
        assertThat(metadataVedlegg2.getJournaltilstand()).isEqualTo(JournalMetadata.Journaltilstand.ENDELIG);
        assertThat(metadataVedlegg2.getVariantFormat()).isEqualTo(variantFormatV2);
        assertThat(metadataVedlegg2.getErHoveddokument()).isFalse();
    }

    @Test
    public void test_hentEnhet_ok()
        throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
        HentJournalpostUgyldigInput, HentJournalpostSikkerhetsbegrensning {

        final String ENHET = "4205";

        Dokumentinnhold dokinnholdHoved = lagDokumentinnhold(arkivFilTypeH, variantFormatH);
        Dokumentinformasjon dokinfoHoved = lagDokumentinformasjon(dokumentKategoriH, dokumenttypeH, DOKUMENT_ID_H, dokinnholdHoved);

        Mottakskanaler mottakskanal = new Mottakskanaler();
        mottakskanal.setValue(mottakKanal.getOffisiellKode());

        InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
        inngaaendeJournalpost.setMottakskanal(mottakskanal);
        inngaaendeJournalpost.setJournaltilstand(Journaltilstand.ENDELIG);
        inngaaendeJournalpost.setHoveddokument(dokinfoHoved);
        ArkivSak sak = new ArkivSak();
        sak.setArkivSakId("123456");
        inngaaendeJournalpost.setArkivSak(sak);

        HentJournalpostResponse response = new HentJournalpostResponse();
        response.setInngaaendeJournalpost(inngaaendeJournalpost);

        when(mockConsumer.hentJournalpost(any(HentJournalpostRequest.class))).thenReturn(response);

        ArkivJournalPost arkivJournalPost = adapter.hentInngåendeJournalpostHoveddokument(JOURNALPOST_ID);

        assertThat(arkivJournalPost.getJournalEnhet()).isEmpty();

        inngaaendeJournalpost.setJournalfEnhet(ENHET);

        ArkivJournalPost arkivJournalPost2 = adapter.hentInngåendeJournalpostHoveddokument(JOURNALPOST_ID);

        assertThat(arkivJournalPost2.getJournalEnhet()).isPresent();
        assertThat(arkivJournalPost2.getJournalEnhet().get()).isEqualTo(ENHET);

    }


    @Test
    public void test_hentMetadata_ufullstendigeObjekter()
        throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
        HentJournalpostUgyldigInput, HentJournalpostSikkerhetsbegrensning {

        Dokumentinnhold dokinnholdHoved = lagDokumentinnhold(null, null);
        Dokumentinformasjon dokinfoHoved = lagDokumentinformasjon(null, null, null, dokinnholdHoved);

        InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
        inngaaendeJournalpost.setHoveddokument(dokinfoHoved);

        HentJournalpostResponse response = new HentJournalpostResponse();
        response.setInngaaendeJournalpost(inngaaendeJournalpost);

        when(mockConsumer.hentJournalpost(any(HentJournalpostRequest.class))).thenReturn(response);

        List<JournalMetadata<DokumentTypeId>> metadataList = adapter.hentMetadata(JOURNALPOST_ID);

        assertThat(metadataList.size()).isEqualTo(1);

        JournalMetadata<DokumentTypeId> metadataHoved = metadataList.get(0);
        assertThat(metadataHoved.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(metadataHoved.getDokumentId()).isNull();
        assertThat(metadataHoved.getArkivFilType()).isNull();
        assertThat(metadataHoved.getDokumentKategori()).isNull();
        assertThat(metadataHoved.getDokumentType()).isNull();
        assertThat(metadataHoved.getMottakKanal()).isNull();
        assertThat(metadataHoved.getJournaltilstand()).isNull();
        assertThat(metadataHoved.getVariantFormat()).isNull();
        assertThat(metadataHoved.getErHoveddokument()).isTrue();
    }

    @Test(expected = IntegrasjonException.class)
    public void test_hentMetadata_HentJournalpostJournalpostIkkeFunnet()
        throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
        HentJournalpostUgyldigInput, HentJournalpostSikkerhetsbegrensning {

        when(mockConsumer.hentJournalpost(any(HentJournalpostRequest.class))).thenThrow(new HentJournalpostJournalpostIkkeFunnet());

        adapter.hentMetadata(JOURNALPOST_ID);

        logSniffer.assertHasErrorMessage("FP-195433:Journalpost ikke funnet");
    }

    @Test(expected = IntegrasjonException.class)
    public void test_hentMetadata_HentJournalpostJournalpostIkkeInngaaende()
        throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
        HentJournalpostUgyldigInput, HentJournalpostSikkerhetsbegrensning {

        when(mockConsumer.hentJournalpost(any(HentJournalpostRequest.class))).thenThrow(new HentJournalpostJournalpostIkkeInngaaende());

        adapter.hentMetadata(JOURNALPOST_ID);

        logSniffer.assertHasErrorMessage("FP-107540:Journalpost ikke inngående");
    }

    @Test(expected = IntegrasjonException.class)
    public void test_hentMetadata_HentJournalpostUgyldigInput()
        throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
        HentJournalpostUgyldigInput, HentJournalpostSikkerhetsbegrensning {

        when(mockConsumer.hentJournalpost(any(HentJournalpostRequest.class))).thenThrow(new HentJournalpostUgyldigInput());

        adapter.hentMetadata(JOURNALPOST_ID);

        logSniffer.assertHasErrorMessage("FP-276411:Journalpost ugyldig input");
    }

    @Test(expected = ManglerTilgangException.class)
    public void test_hentMetadata_HentJournalpostSikkerhetsbegrensning()
        throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
        HentJournalpostUgyldigInput, HentJournalpostSikkerhetsbegrensning {

        when(mockConsumer.hentJournalpost(any(HentJournalpostRequest.class))).thenThrow(new HentJournalpostSikkerhetsbegrensning());

        adapter.hentMetadata(JOURNALPOST_ID);

        logSniffer.assertHasErrorMessage("FP-432999:Journal ikke tilgjengelig (sikkerhetsbegrensning)");
    }

    private Dokumentinformasjon lagDokumentinformasjon(
        DokumentKategori dokumentKategori, DokumentTypeId dokumentTypeId, String dokumentId, Dokumentinnhold... dokumentinnholdArray) {

        Dokumentinformasjon dokinfo = new Dokumentinformasjon();

        if (dokumentKategori != null) {
            Dokumentkategorier dokumentkategori = new Dokumentkategorier();
            dokumentkategori.setValue(dokumentKategori.getOffisiellKode());
            dokinfo.setDokumentkategori(dokumentkategori);
        }
        if (dokumentTypeId != null) {
            DokumenttypeIder dokumenttypeId = new DokumenttypeIder();
            dokumenttypeId.setValue(dokumentTypeId.getOffisiellKode());
            dokinfo.setDokumenttypeId(dokumenttypeId);
        }
        dokinfo.setDokumentId(dokumentId);
        for (Dokumentinnhold dokumentinnhold : dokumentinnholdArray) {
            dokinfo.getDokumentInnholdListe().add(dokumentinnhold);
        }

        return dokinfo;
    }

    private Dokumentinnhold lagDokumentinnhold(ArkivFilType arkivFilType, VariantFormat variantFormat) {

        Dokumentinnhold dokumentinnhold = new Dokumentinnhold();

        if (arkivFilType != null) {
            Arkivfiltyper arkivfiltype = new Arkivfiltyper();
            arkivfiltype.setValue(arkivFilType.getOffisiellKode());
            dokumentinnhold.setArkivfiltype(arkivfiltype);
        }
        if (variantFormat != null) {
            Variantformater variantformat = new Variantformater();
            variantformat.setValue(variantFormat.getOffisiellKode());
            dokumentinnhold.setVariantformat(variantformat);
        }

        return dokumentinnhold;
    }

}
