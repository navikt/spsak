package no.nav.sykepenger.kafka;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.fordeling.JournalpostMottakDto;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class InntektsmeldingConsumer extends KafkaConsumer {

    private SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste;
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepository behandlingRepository;

    InntektsmeldingConsumer() {
        // CDI
    }

    @Inject
    public InntektsmeldingConsumer(SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste,
                                   DokumentArkivTjeneste dokumentArkivTjeneste,
                                   FagsakTjeneste fagsakTjeneste,
                                   BehandlingRepositoryProvider repositoryProvider) {
        super("inntektsmelding");

        this.dokumentmottakTjeneste = dokumentmottakTjeneste;
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Override
    protected void handleMessage(String key, String payload) {
        System.out.println("KEY=" + key + ", PAYLOAD=" + payload);
        JSONObject json = new JSONObject(payload);
        // TODO: Sanity-check på json-struktur ++
        JournalpostMottakDto mottattJournalpost = new JournalpostMottakDto(
            Long.toString(json.getLong("saksnummer")),
            json.getString("journalpostId"),
            "ab0061", //json.getString("behandlingstemaOffisiellKode"),
            "I000067", // INNTEKSTMELDING
            LocalDate.now(),
            new String(Base64.getDecoder().decode(json.getString("xml")), Charset.forName("UTF-8")));
        InngåendeSaksdokument saksdokument = map(mottattJournalpost);
        dokumentmottakTjeneste.dokumentAnkommet(saksdokument);
    }

    private InngåendeSaksdokument map(JournalpostMottakDto mottattJournalpost) {
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
        dokumentKategori = utledDokumentKategori(dokumentKategori, dokumentTypeId);

        InngåendeSaksdokument.Builder builder = InngåendeSaksdokument.builder()
            .medFagsakId(fagsak.get().getId())
            .medBehandlingTema(behandlingTema)
            .medElektroniskSøknad(mottattJournalpost.getPayloadXml().isPresent())
            .medJournalpostId(new JournalpostId(mottattJournalpost.getJournalpostId()))
            .medDokumentTypeId(dokumentTypeId)
            .medDokumentKategori(dokumentKategori)
            .medJournalførendeEnhet(mottattJournalpost.getJournalForendeEnhet());

        mottattJournalpost.getForsendelseId().ifPresent(builder::medForsendelseId);

        if (mottattJournalpost.getPayloadXml().isPresent()) {
            builder.medPayloadXml(mottattJournalpost.getPayloadXml().get()); // NOSONAR
        }

        builder.medForsendelseMottatt(mottattJournalpost.getForsendelseMottatt().orElse(FPDateUtil.iDag())); // NOSONAR

        return builder.build();
    }

    private DokumentTypeId utledDokumentTypeId(Saksnummer saksnummer, JournalpostId journalpostId, DokumentTypeId dokumentTypeId) {
        if (!DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return dokumentTypeId;
        }
        return dokumentArkivTjeneste.utledDokumentTypeFraTittel(saksnummer, journalpostId);
    }

    private DokumentKategori utledDokumentKategori(DokumentKategori dokumentKategori, DokumentTypeId dokumentTypeId) {
        if (DokumentTypeId.getSøknadTyper().contains(dokumentTypeId)) {
            return DokumentKategori.SØKNAD;
        }
        if (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)) {
            return DokumentKategori.KLAGE_ELLER_ANKE;
        }
        return dokumentKategori;
    }

}
