package no.nav.sykepenger.kafka;

import java.util.Optional;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.PayloadType;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.fordeling.JournalpostMottakDto;
import no.nav.vedtak.util.FPDateUtil;

class MottakTilSaksdokumentMapper {
    private final KodeverkRepository kodeverkRepository;
    private final FagsakTjeneste fagsakTjeneste;
    private final DokumentArkivTjeneste dokumentArkivTjeneste;

    MottakTilSaksdokumentMapper(KodeverkRepository kodeverkRepository, FagsakTjeneste fagsakTjeneste, DokumentArkivTjeneste dokumentArkivTjeneste) {
        this.kodeverkRepository = kodeverkRepository;
        this.fagsakTjeneste = fagsakTjeneste;
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
    }

    InngåendeSaksdokument map(JournalpostMottakDto mottattJournalpost, PayloadType type) {
        BehandlingTema behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, mottattJournalpost.getBehandlingstemaOffisiellKode(),
            BehandlingTema.UDEFINERT);
        JournalpostId journalpostId = new JournalpostId(mottattJournalpost.getJournalpostId());

        Saksnummer saksnummer = new Saksnummer(mottattJournalpost.getSaksnummer());
        Optional<Fagsak> fagsak = fagsakTjeneste.finnFagsakGittSaksnummer(saksnummer, false);
        if (!fagsak.isPresent()) {
            // FIXME (u139158): PK- hvordan skal dette håndteres?
            // throw BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(removeLineBreaks(saksnummer.toString())).toException();
            throw new IllegalStateException("Finner ingen fagsak for saksnummer " + saksnummer);
        }

        DokumentTypeId dokumentTypeId = mottattJournalpost.getDokumentTypeIdOffisiellKode().isPresent() ?
            kodeverkRepository.finnForKodeverkEiersKode(DokumentTypeId.class, mottattJournalpost.getDokumentTypeIdOffisiellKode().get(), DokumentTypeId.UDEFINERT) :
            DokumentTypeId.UDEFINERT; // NOSONAR

        DokumentKategori dokumentKategori = mottattJournalpost.getDokumentKategoriOffisiellKode() != null ?
            kodeverkRepository.finnForKodeverkEiersKode(DokumentKategori.class, mottattJournalpost.getDokumentKategoriOffisiellKode(), DokumentKategori.UDEFINERT) :
            DokumentKategori.UDEFINERT; // NOSONAR

        dokumentTypeId = utledDokumentTypeId(saksnummer, journalpostId, dokumentTypeId);
        dokumentKategori = DokumentTypeId.utledDokumentKategori(dokumentKategori, dokumentTypeId);

        InngåendeSaksdokument.Builder builder = InngåendeSaksdokument.builder()
            .medFagsakId(fagsak.get().getId())
            .medBehandlingTema(behandlingTema)
            .medJournalpostId(new JournalpostId(mottattJournalpost.getJournalpostId()))
            .medDokumentTypeId(dokumentTypeId)
            .medDokumentKategori(dokumentKategori)
            .medJournalførendeEnhet(mottattJournalpost.getJournalForendeEnhet());

        mottattJournalpost.getForsendelseId().ifPresent(builder::medForsendelseId);

        if (mottattJournalpost.getPayload().isPresent()) {
            builder.medPayload(type, mottattJournalpost.getPayload().get()); // NOSONAR
        }

        builder.medMottattDato(mottattJournalpost.getForsendelseMottatt().orElse(FPDateUtil.iDag())); // NOSONAR

        return builder.build();
    }

    private DokumentTypeId utledDokumentTypeId(Saksnummer saksnummer, JournalpostId journalpostId, DokumentTypeId dokumentTypeId) {
        if (!DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return dokumentTypeId;
        }
        return DokumentTypeId.UDEFINERT;
    }


}
