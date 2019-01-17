package no.nav.foreldrepenger.fordel.dokument.v1;

import static no.nav.vedtak.log.util.LoggerUtils.removeLineBreaks;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.task.KlargjorForVLTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.KonfigVerdiTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.BehandleDokumentforsendelseV1;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.feil.WSJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.feil.WSUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.WSOppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

/**
 * Webservice for å oppdatere og ferdigstille journalføring. For så å klargjøre og sende over saken til videre behandling i VL.
 */

@Dependent
@WebService(
        wsdlLocation = "wsdl/no/nav/tjeneste/virksomhet/behandleDokumentforsendelse/v1/behandleDokumentforsendelse.wsdl",
        serviceName = "BehandleDokumentforsendelse_v1",
        portName = "BehandleDokumentforsendelse_v1Port",
        endpointInterface = "no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.binding.BehandleDokumentforsendelseV1")
@SoapWebService(endpoint = "/sak/behandleDokument/v1", tjenesteBeskrivelseURL = "https://confluence.adeo.no/pages/viewpage.action?pageId=220529141")
public class BehandleDokumentService implements BehandleDokumentforsendelseV1 {

    public static final Logger logger = LoggerFactory.getLogger(BehandleDokumentService.class);

    static final String JOURNALPOST_MANGLER = "JournalpostId mangler";
    static final String ENHET_MANGLER = "EnhetId mangler";
    static final String SAKSNUMMER_UGYLDIG = "SakId (saksnummer) mangler eller er ugyldig";
    static final String KLAGE_UTEN_BEHANDLING = "Klager må journalføres på sak med tidligere behandling";
    static final String INNTEKTSMELDING_FEIL_YTELSE = "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre";
    static final String INNTEKTSMELDING_MANGLER_STARTDATO = "Inntektsmelding mangler startdato - kan ikke journalføre";
    static final String FOR_TIDLIG_UTTAK = "Søknad om uttak med oppstart i 2018 skal journalføres mot sak i Infotrygd";

    private TilJournalføringTjeneste tilJournalføringTjeneste;
    private HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste;
    private KlargjørForVLTjeneste klargjørForVLTjeneste;
    private FagsakRestKlient fagsakRestKlient;
    private KodeverkRepository kodeverkRepository;
    private KonfigVerdiTjeneste konfigVerdiTjeneste;
    private AktørConsumer aktørConsumer;

    @Inject
    public BehandleDokumentService(TilJournalføringTjeneste tilJournalføringTjeneste, HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste,
                                   KlargjørForVLTjeneste klargjørForVLTjeneste, FagsakRestKlient fagsakRestKlient, KodeverkRepository kodeverkRepository,
                                   KonfigVerdiTjeneste konfigVerdiTjeneste, AktørConsumer aktørConsumer) {
        this.tilJournalføringTjeneste = tilJournalføringTjeneste;
        this.hentDataFraJoarkTjeneste = hentDataFraJoarkTjeneste;
        this.klargjørForVLTjeneste = klargjørForVLTjeneste;
        this.fagsakRestKlient = fagsakRestKlient;
        this.kodeverkRepository = kodeverkRepository;
        this.konfigVerdiTjeneste = konfigVerdiTjeneste;
        this.aktørConsumer = aktørConsumer;
    }

    public BehandleDokumentService() {
        //NOSONAR: for cdi
    }

    @Override
    public void ping() {
        logger.debug(removeLineBreaks("ping")); //NOSONAR
    }

    @Override
    @Transaction
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public void oppdaterOgFerdigstillJournalfoering(
            @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class)
                    WSOppdaterOgFerdigstillJournalfoeringRequest request)
            throws OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet, OppdaterOgFerdigstillJournalfoeringSikkerhetsbegrensning, OppdaterOgFerdigstillJournalfoeringUgyldigInput {

        final String saksnummer = request.getSakId();
        validerSaksnummer(saksnummer);

        final String arkivId = request.getJournalpostId();
        validerArkivId(arkivId);

        final String enhetId = request.getEnhetId();
        validerEnhetId(enhetId);

        Optional<FagsakInfomasjonDto> optFagsakInfomasjonDto = fagsakRestKlient.finnFagsakInfomasjon(new SaksnummerDto(saksnummer));
        if (!optFagsakInfomasjonDto.isPresent()) {
            throw BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(saksnummer).toException();
        }

        FagsakInfomasjonDto fagsakInfomasjonDto = optFagsakInfomasjonDto.get();
        String behandlingstemaOffisiellKode = fagsakInfomasjonDto.getBehandlingstemaOffisiellKode();
        BehandlingTema behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, behandlingstemaOffisiellKode, BehandlingTema.UDEFINERT);
        String aktørId = fagsakInfomasjonDto.getAktørId();

        Optional<JournalMetadata<DokumentTypeId>> optJournalMetadata = hentDataFraJoarkTjeneste.hentHoveddokumentMetadata(arkivId);
        if (!optJournalMetadata.isPresent()) {
            WSJournalpostIkkeFunnet journalpostIkkeFunnet = new WSJournalpostIkkeFunnet();
            journalpostIkkeFunnet.setFeilmelding("Finner ikke journalpost med id " + arkivId);
            journalpostIkkeFunnet.setFeilaarsak("Finner ikke journalpost");
            throw new OppdaterOgFerdigstillJournalfoeringJournalpostIkkeFunnet(journalpostIkkeFunnet.getFeilmelding(), journalpostIkkeFunnet);
        }
        final JournalMetadata<DokumentTypeId> journalMetadata = optJournalMetadata.get();
        final DokumentTypeId dokumentTypeId = journalMetadata.getDokumentTypeId() != null ? kodeverkRepository.finn(DokumentTypeId.class, journalMetadata.getDokumentTypeId()) : DokumentTypeId.UDEFINERT;
        final DokumentKategori dokumentKategori = journalMetadata.getDokumentKategori() != null ? kodeverkRepository.finn(DokumentKategori.class, journalMetadata.getDokumentKategori()) : DokumentKategori.UDEFINERT;
        behandlingTema = kodeverkRepository.finn(BehandlingTema.class, HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, behandlingTema, dokumentTypeId));

        if (BehandlingTema.UDEFINERT.equals(behandlingTema) && (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId) || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokumentKategori))) {
            WSUgyldigInput ugyldigInput = lagUgyldigInput(KLAGE_UTEN_BEHANDLING);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }

        Optional<JournalDokument<DokumentTypeId>> journalDokument = hentDataFraJoarkTjeneste.hentStrukturertJournalDokument(journalMetadata);
        final String xml = journalDokument.map(JournalDokument::getXml).orElse(null);
        if (xml != null) {
            // Bruker eksisterende infrastruktur for å hente ut og validere XML-data. Tasktype tilfeldig valgt
            ProsessTaskData prosessTaskData = new ProsessTaskData(KlargjorForVLTask.TASKNAME);
            MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);
            dataWrapper.setBehandlingTema(behandlingTema);
            dataWrapper.setSaksnummer(saksnummer);
            dataWrapper.setAktørId(aktørId);
            validerXml(dataWrapper, behandlingTema, dokumentTypeId, xml);
        }

        if (!JournalMetadata.Journaltilstand.ENDELIG.equals(journalMetadata.getJournaltilstand())) {
            logger.info(removeLineBreaks("Kaller tilJournalføring")); //NOSONAR
            tilJournalføringTjeneste.tilJournalføring(arkivId, saksnummer, aktørId, enhetId);
        }

        klargjørForVLTjeneste.klargjørForVL(xml, saksnummer, arkivId, dokumentTypeId, journalMetadata.getForsendelseMottatt(),
                behandlingTema, null, dokumentKategori, journalMetadata.getJournalførendeEnhet()); //TODO: Shekhar forsendelseid null
    }

    private void validerSaksnummer(String saksnummer) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (erNullEllerTom(saksnummer)) {
            WSUgyldigInput ugyldigInput = lagUgyldigInput(SAKSNUMMER_UGYLDIG);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private void validerArkivId(String arkivId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (erNullEllerTom(arkivId)) {
            WSUgyldigInput ugyldigInput = lagUgyldigInput(JOURNALPOST_MANGLER);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private void validerEnhetId(String enhetId) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        if (enhetId == null) {
            WSUgyldigInput ugyldigInput = lagUgyldigInput(ENHET_MANGLER);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private boolean erNullEllerTom(String s) {
        return (s == null || s.isEmpty());
    }

    private void validerXml(MottakMeldingDataWrapper dataWrapper, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, String xml) throws OppdaterOgFerdigstillJournalfoeringUgyldigInput {
        MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(xml);
        mottattDokument.kopierTilMottakWrapper(dataWrapper, aktørConsumer::hentAktørIdForPersonIdent);
        String imType = dataWrapper.getInntektsmeldingYtelse().orElse(null);
        LocalDate startDato = dataWrapper.getFørsteUttaksdag().orElse(null);
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            BehandlingTema behandlingTemaFraIM = kodeverkRepository.finnForKodeverkEiersTermNavn(BehandlingTema.class, imType, BehandlingTema.UDEFINERT);
            if (BehandlingTema.gjelderForeldrepenger(behandlingTemaFraIM)) {
                if (!dataWrapper.getInntektsmeldingStartDato().isPresent()) { // Kommer ingen vei uten startdato
                    WSUgyldigInput ugyldigInput = lagUgyldigInput(INNTEKTSMELDING_MANGLER_STARTDATO);
                    throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
                } else if (!BehandlingTema.gjelderForeldrepenger(behandlingTema)) { // Prøver journalføre på annen fagsak - ytelsetype
                    WSUgyldigInput ugyldigInput = lagUgyldigInput(INNTEKTSMELDING_FEIL_YTELSE);
                    throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
                }
            } else if (!behandlingTemaFraIM.equals(behandlingTema)) { // IM Svangerskapspenger
                WSUgyldigInput ugyldigInput = lagUgyldigInput(INNTEKTSMELDING_FEIL_YTELSE);
                throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
            }
        }
        if (dokumentTypeId.erForeldrepengerRelatert() && startDato != null && startDato.isBefore(konfigVerdiTjeneste.getKonfigVerdiStartdatoForeldrepenger())) {
            WSUgyldigInput ugyldigInput = lagUgyldigInput(FOR_TIDLIG_UTTAK);
            throw new OppdaterOgFerdigstillJournalfoeringUgyldigInput(ugyldigInput.getFeilmelding(), ugyldigInput);
        }
    }

    private WSUgyldigInput lagUgyldigInput(String melding) {
        WSUgyldigInput faultInfo = new WSUgyldigInput();
        faultInfo.setFeilmelding(melding);
        faultInfo.setFeilaarsak("Ugyldig input");
        return faultInfo;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            WSOppdaterOgFerdigstillJournalfoeringRequest req = (WSOppdaterOgFerdigstillJournalfoeringRequest) obj;
            return AbacDataAttributter.opprett()
                    .leggTilSaksnummer(req.getSakId())
                    .leggTilJournalPostId(req.getJournalpostId(), false);
        }
    }
}
