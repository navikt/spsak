package no.nav.foreldrepenger.mottak.tjeneste;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.journal.JournalPost;
import no.nav.foreldrepenger.mottak.journal.JournalPostMangler;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;

@ApplicationScoped
public class TilJournalføringTjeneste {

    private static final Logger log = LoggerFactory.getLogger(TilJournalføringTjeneste.class);

    private JournalTjeneste journalTjeneste;
    private FagsakRestKlient fagsakRestKlient;
    private DokumentRepository dokumentRepository;

    @Inject
    public TilJournalføringTjeneste(JournalTjeneste journalTjeneste, FagsakRestKlient fagsakRestKlient, DokumentRepository dokumentRepository) {
        this.journalTjeneste = journalTjeneste;
        this.fagsakRestKlient = fagsakRestKlient;
        this.dokumentRepository = dokumentRepository;
    }

    public TilJournalføringTjeneste() {
        //NOSONAR for cdi
    }

    public void tilJournalføring(String journalpostId, String sakId, String aktørId, String enhetId) {

        fagsakRestKlient.knyttSakOgJournalpost(new JournalpostKnyttningDto(sakId, journalpostId));

        final JournalPostMangler journalføringsbehov = journalTjeneste.utledJournalføringsbehov(journalpostId);

        if (journalføringsbehov.harMangler()) {
            if (retteOppMangler(sakId, journalpostId, aktørId, journalføringsbehov)) {
                throw MottakMeldingFeil.FACTORY.kanIkkeRetteOppJournalmangler(journalpostId).toException();
            }
        }
        journalTjeneste.ferdigstillJournalføring(journalpostId, enhetId);
    }

    public DokumentforsendelseResponse journalførDokumentforsendelse(UUID forsendelseId,
                                                                     Optional<String> saksnummer,
                                                                     Optional<String> avsenderId,
                                                                     Boolean forsøkEndeligJF, boolean retrying) {
        DokumentMetadata metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        List<Dokument> dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        List<Dokument> hoveddokument = dokumenter.stream().filter(dokument -> dokument.erHovedDokument()).collect(Collectors.toList());
        List<Dokument> vedlegg = dokumenter.stream().filter(dokument -> !dokument.erHovedDokument()).collect(Collectors.toList());

        if (forsøkEndeligJF && !saksnummer.isPresent()) {
            throw MottakMeldingFeil.FACTORY.manglerSaksnummerForJournalføring(forsendelseId).toException();
        }

        DokumentforsendelseRequest.Builder builder = DokumentforsendelseRequest.builder();
        builder.medForsøkEndeligJF(forsøkEndeligJF);
        builder.medForsendelseId(metadata.getForsendelseId().toString());
        builder.medBruker(metadata.getBrukerId());
        builder.medForsendelseMottatt(metadata.getForsendelseMottatt());
        if (retrying) {
            builder.medRetrying();
        }
        builder.medHoveddokument(hoveddokument);
        builder.medVedlegg(vedlegg);

        saksnummer.ifPresent(builder::medSaksnummer);

        if (avsenderId.isPresent()) {
            builder.medAvsender(avsenderId.get());
        } else {
            builder.medAvsender(metadata.getBrukerId());
        }

        DokumentforsendelseResponse response = journalTjeneste.journalførDokumentforsendelse(builder.build());

        if (forsøkEndeligJF) {
            if (!JournalTilstand.ENDELIG_JOURNALFØRT.equals(response.getJournalTilstand())) {
                throw MottakMeldingFeil.FACTORY.feilJournalTilstandForventetTilstandEndelig(response.getJournalTilstand()).toException();
            }
            String sakId = saksnummer.orElseThrow(() -> MottakMeldingFeil.FACTORY.manglerSaksnummerForJournalføring(forsendelseId).toException());
            fagsakRestKlient.knyttSakOgJournalpost(new JournalpostKnyttningDto(sakId, response.getJournalpostId()));
        }
        return response;
    }

    private boolean retteOppMangler(String sakId, String arkivId, String aktørId, JournalPostMangler journalføringsbehov) {
        final JournalPost journalPost = new JournalPost(arkivId);
        boolean arkivMangler = true; // Skal som regel mangle når den journalføres her
        List<JournalPostMangler.JournalMangelType> manglene = journalføringsbehov.getMangler();
        for (JournalPostMangler.JournalMangelType mangel : manglene) {
            switch (mangel) {
                case ARKIVSAK:
                    journalPost.setArkivSakId(sakId);
                    journalPost.setArkivSakSystem(Fagsystem.GOSYS.getOffisiellKode());
                    journalføringsbehov.rettetMangel(mangel);
                    arkivMangler = false;
                    break;
                case AVSENDERID:
                    journalPost.setAvsenderAktørId(aktørId);
                    journalføringsbehov.rettetMangel(mangel);
                    break;
                case AVSENDERNAVN:
                    break;
                case INNHOLD:
                    break;
                case TEMA:
                    break;
                case BRUKER:
                    journalPost.setAktørId(aktørId);
                    journalføringsbehov.rettetMangel(mangel);
                    break;
                default:
                    // Too be implemented
                    break;
            }
        }
        if (arkivMangler) {
            log.warn("Journalpost utledete mangler inkluderer ikke arkivsak {} mangler {}", arkivId, manglene.toString());
        }

        journalTjeneste.oppdaterJournalpost(journalPost);

        return journalføringsbehov.harMangler();
    }
}


