package no.nav.foreldrepenger.domene.dokumentarkiv.journal.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.Kommunikasjonsretning;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.InngåendeJournalAdapter;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalMetadata;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinnhold;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.DokumenttypeIder;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Mottakskanaler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalConsumer;

@ApplicationScoped
public class InngåendeJournalAdapterImpl implements InngåendeJournalAdapter {

    private InngaaendeJournalConsumer consumer;
    private KodeverkRepository kodeverkRepository;

    private static Map<Journaltilstand, JournalMetadata.Journaltilstand> journaltilstandPrjournaltilstandJaxb;

    static {
        journaltilstandPrjournaltilstandJaxb = new EnumMap<>(Journaltilstand.class);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.MIDLERTIDIG, JournalMetadata.Journaltilstand.MIDLERTIDIG);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.UTGAAR, JournalMetadata.Journaltilstand.UTGAAR);
        journaltilstandPrjournaltilstandJaxb.put(Journaltilstand.ENDELIG, JournalMetadata.Journaltilstand.ENDELIG);
    }

    InngåendeJournalAdapterImpl() {
        // CDI
    }

    @Inject
    public InngåendeJournalAdapterImpl(InngaaendeJournalConsumer consumer, KodeverkRepository kodeverkRepository) {
        this.consumer = consumer;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public List<JournalMetadata<DokumentTypeId>> hentMetadata(JournalpostId journalpostId) {
        HentJournalpostResponse response = doHentJournalpost(journalpostId);
        List<JournalMetadata<DokumentTypeId>> metadataList = new ArrayList<>();
        konverterTilMetadata(journalpostId, response, metadataList);
        return metadataList;
    }

    @Override
    public ArkivJournalPost hentInngåendeJournalpostHoveddokument(JournalpostId journalpostId) {
        InngaaendeJournalpost response = doHentJournalpost(journalpostId).getInngaaendeJournalpost();
        Dokumentinformasjon hoved = response.getHoveddokument();
        ArkivDokument.Builder dokBuilder = ArkivDokument.Builder.ny()
            .medDokumentKategori(hoved.getDokumentkategori() != null ? kodeverkRepository.finnForKodeverkEiersKode(DokumentKategori.class, hoved.getDokumentkategori().getValue(), DokumentKategori.UDEFINERT) : DokumentKategori.UDEFINERT)
            .medDokumentTypeId(hoved.getDokumenttypeId() != null ? kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, hoved.getDokumenttypeId().getValue(), DokumentTypeId.UDEFINERT) : DokumentTypeId.UDEFINERT)
            .medDokumentId(hoved.getDokumentId())
            .medTittel("");

        ArkivJournalPost arkivJournalPost = ArkivJournalPost.Builder.ny()
            .medSaksnummer(response.getArkivSak() != null ? new Saksnummer(response.getArkivSak().getArkivSakId()) : null)
            .medJournalpostId(journalpostId)
            .medTidspunkt(DateUtil.convertToLocalDate(response.getForsendelseMottatt()))
            .medKommunikasjonsretning(Kommunikasjonsretning.INN)
            .medBeskrivelse("")
            .medJournalFørendeEnhet(response.getJournalfEnhet())
            .medHoveddokument(dokBuilder.build())
            .build();

        return arkivJournalPost;
    }

    private HentJournalpostResponse doHentJournalpost(JournalpostId journalpostId) {

        HentJournalpostRequest request = new HentJournalpostRequest();
        request.setJournalpostId(journalpostId.getVerdi());

        HentJournalpostResponse response;
        try {
            response = consumer.hentJournalpost(request);
        } catch (HentJournalpostJournalpostIkkeFunnet e) {
            throw JournalFeil.FACTORY.hentJournalpostIkkeFunnet(e).toException();
        } catch (HentJournalpostSikkerhetsbegrensning e) {
            throw JournalFeil.FACTORY.journalUtilgjengeligSikkerhetsbegrensning("Hent metadata", e).toException();
        } catch (HentJournalpostUgyldigInput e) {
            throw JournalFeil.FACTORY.journalpostUgyldigInput(e).toException();
        } catch (HentJournalpostJournalpostIkkeInngaaende e) {
            throw JournalFeil.FACTORY.journalpostIkkeInngaaende(e).toException();
        }

        return response;
    }

    private void konverterTilMetadata(JournalpostId journalpostId, HentJournalpostResponse response, List<JournalMetadata<DokumentTypeId>> metadataList) {
        InngaaendeJournalpost journalpost = response.getInngaaendeJournalpost();

        konverterTilMetadata(journalpostId, journalpost, journalpost.getHoveddokument(), true, metadataList);
        if (journalpost.getVedleggListe() != null) {
            for (Dokumentinformasjon dokumentinfo : journalpost.getVedleggListe()) {
                konverterTilMetadata(journalpostId, journalpost, dokumentinfo, false, metadataList);
            }
        }
    }

    private void konverterTilMetadata(JournalpostId journalpostId, InngaaendeJournalpost journalpost,
                                      Dokumentinformasjon dokumentinfo, boolean erHoveddokument,
                                      List<JournalMetadata<DokumentTypeId>> metadataList) {

        MottakKanal mottakKanal = getMottakKanal(journalpost);

        Journaltilstand journaltilstandJaxb = journalpost.getJournaltilstand();
        JournalMetadata.Journaltilstand journaltilstand = journaltilstandJaxb != null ? journaltilstandPrjournaltilstandJaxb.get(journaltilstandJaxb) : null;

        LocalDate forsendelseMottatt = DateUtil.convertToLocalDate(journalpost.getForsendelseMottatt());

        List<Aktoer> brukerListe = journalpost.getBrukerListe();

        final String dokumentId = dokumentinfo.getDokumentId();
        final DokumentTypeId dokumentTypeId = getDokumentTypeId(dokumentinfo);
        final DokumentKategori dokumentKategori = getDokumentKategori(dokumentinfo);

        List<String> brukerIdentList = brukerListe.stream().filter((a) -> {
            // instanceof OK - eksternt grensesnitt
            return a instanceof Person;  // NOSONAR
        }).map(a -> ((Person) a).getIdent()).collect(Collectors.toList());

        for (Dokumentinnhold dokumentinnhold : dokumentinfo.getDokumentInnholdListe()) {
            VariantFormat variantFormat = getVariantFormat(dokumentinnhold);
            ArkivFilType arkivFilType = getArkivFilType(dokumentinnhold);

            JournalMetadata.Builder<DokumentTypeId> builder = JournalMetadata.builder();
            builder.medJournalpostId(journalpostId);
            builder.medDokumentId(dokumentId);
            builder.medVariantFormat(variantFormat);
            builder.medMottakKanal(mottakKanal);
            builder.medDokumentType(dokumentTypeId);
            builder.medDokumentKategori(dokumentKategori);
            builder.medArkivFilType(arkivFilType);
            builder.medJournaltilstand(journaltilstand);
            builder.medErHoveddokument(erHoveddokument);
            builder.medForsendelseMottatt(forsendelseMottatt);
            builder.medBrukerIdentListe(brukerIdentList);
            JournalMetadata<DokumentTypeId> metadata = builder.build();

            metadataList.add(metadata);
        }
    }

    private MottakKanal getMottakKanal(InngaaendeJournalpost journalpost) {
        MottakKanal mottakKanal = null;
        Mottakskanaler mottakskanalJaxb = journalpost.getMottakskanal();
        if (mottakskanalJaxb != null && mottakskanalJaxb.getValue() != null) {
            String offisiellKode = mottakskanalJaxb.getValue();
            mottakKanal = kodeverkRepository.finnForKodeverkEiersKode(MottakKanal.class, offisiellKode);
        }
        return mottakKanal;
    }

    private DokumentTypeId getDokumentTypeId(Dokumentinformasjon dokumentinfo) {
        DokumentTypeId dokumentTypeId = null;
        DokumenttypeIder dokumenttypeJaxb = dokumentinfo.getDokumenttypeId();
        if (dokumenttypeJaxb != null && dokumenttypeJaxb.getValue() != null) {
            final String offisiellKode = dokumenttypeJaxb.getValue();
            dokumentTypeId = HentDokumentType.slåOppInngåendeDokumentType(kodeverkRepository, offisiellKode);
        }
        return dokumentTypeId;
    }

    private DokumentKategori getDokumentKategori(Dokumentinformasjon dokumentinfo) {
        DokumentKategori dokumentKategori = null;
        Dokumentkategorier dokumentkategoriJaxb = dokumentinfo.getDokumentkategori();
        if (dokumentkategoriJaxb != null && dokumentkategoriJaxb.getValue() != null) {
            String offisiellKode = dokumentkategoriJaxb.getValue();
            dokumentKategori = kodeverkRepository.finnForKodeverkEiersKode(DokumentKategori.class, offisiellKode);
        }
        return dokumentKategori;
    }

    private VariantFormat getVariantFormat(Dokumentinnhold dokumentinnhold) {
        VariantFormat variantFormat = null;
        Variantformater variantformatJaxb = dokumentinnhold.getVariantformat();
        if (variantformatJaxb != null && variantformatJaxb.getValue() != null) {
            String offisiellKode = variantformatJaxb.getValue();
            variantFormat = kodeverkRepository.finnForKodeverkEiersKode(VariantFormat.class, offisiellKode);
        }
        return variantFormat;
    }

    private ArkivFilType getArkivFilType(Dokumentinnhold dokumentinnhold) {
        ArkivFilType arkivFilType = null;
        Arkivfiltyper arkivfiltypeJaxb = dokumentinnhold.getArkivfiltype();
        if (arkivfiltypeJaxb != null && arkivfiltypeJaxb.getValue() != null) {
            String offisiellKode = arkivfiltypeJaxb.getValue();
            arkivFilType = kodeverkRepository.finnForKodeverkEiersKode(ArkivFilType.class, offisiellKode);
        }
        return arkivFilType;
    }
}
