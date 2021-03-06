package no.nav.foreldrepenger.mottak.task.dokumentforsendelse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.foreldrepenger.mottak.task.OpprettSakTask;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

@Dependent
@ProsessTask(BehandleDokumentforsendelseTask.TASKNAME)
public class BehandleDokumentforsendelseTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.behandleDokumentForsendelse";

    private AktørConsumer aktørConsumer;
    private FagsakRestKlient fagsakRestKlient;
    private DokumentRepository dokumentRepository;
    private String konfigVerdiStartdatoForeldrepenger;

    @Inject
    public BehandleDokumentforsendelseTask(ProsessTaskRepository prosessTaskRepository,
                                           KodeverkRepository kodeverkRepository,
                                           AktørConsumer aktørConsumer,
                                           FagsakRestKlient fagsakRestKlient,
                                           DokumentRepository dokumentRepository,
                                           @KonfigVerdi(value = "foreldrepenger.startdato") String konfigVerdiStartdatoForeldrepenger) {
        super(prosessTaskRepository, kodeverkRepository);
        this.aktørConsumer = aktørConsumer;
        this.fagsakRestKlient = fagsakRestKlient;
        this.dokumentRepository = dokumentRepository;
        this.konfigVerdiStartdatoForeldrepenger = konfigVerdiStartdatoForeldrepenger;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getForsendelseId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getBehandlingTema() == null) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY, dataWrapper.getId()).toException();
        }

        if (TilJournalføringTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            postconditionJournalføring(dataWrapper);
        }

        if (HentOgVurderVLSakTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType()) ||
                OpprettSakTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())) {
            postConditionHentOgVurderVLSakOgOpprettSak(dataWrapper);
        }
    }

    private void postconditionJournalføring(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getSaksnummer().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {

        UUID forsendelseId = dataWrapper.getForsendelseId().get(); // NOSONAR verifisert at finnes i precondition

        Optional<Dokument> hovedDokumentOpt = dokumentRepository.hentUnikDokument(forsendelseId, true, ArkivFilType.XML);
        DokumentMetadata metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);

        AtomicReference<Dokument> dokument = new AtomicReference<>();
        AtomicReference<BehandlingTema> behandlingTema = new AtomicReference<>();

        hovedDokumentOpt.ifPresent(dokument::set);
        behandlingTema.set(HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(dataWrapper.getHarTema() ? dataWrapper.getTema() : null, BehandlingTema.UDEFINERT, hovedDokumentOpt.map(Dokument::getDokumentTypeId).orElse(DokumentTypeId.UDEFINERT)));
        dataWrapper.setBehandlingTema(kodeverkRepository.finn(BehandlingTema.class, behandlingTema.get()));

        setFellesWrapperAttributter(dataWrapper, dokument.get(), metadata);

        // Nytt dokument for eksisterende sak
        if (metadata.getSaksnummer().isPresent()) {
            String saksnr = metadata.getSaksnummer().get(); // NOSONAR
            Optional<FagsakInfomasjonDto> fagInfoOpt = fagsakRestKlient.finnFagsakInfomasjon(new SaksnummerDto(saksnr));
            if (fagInfoOpt.isPresent()) {
                setFellesWrapperAttributterFraFagsak(dataWrapper, fagInfoOpt.get(), hovedDokumentOpt);
                return dataWrapper.nesteSteg(TilJournalføringTask.TASKNAME);
            } else {
                return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
        }

        if (BehandlingTema.gjelderEngangsstønad(behandlingTema.get())
                || BehandlingTema.gjelderForeldrepenger(behandlingTema.get())
                && !sjekkOmSøknadenKreverManuellBehandling(dataWrapper.getFørsteUttaksdag())) {
            return dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
        }
        return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
    }

    private void postConditionHentOgVurderVLSakOgOpprettSak(MottakMeldingDataWrapper dataWrapper) {
        if (OpprettSakTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType()) && !dataWrapper.getForsendelseMottattTidspunkt().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.FORSENDELSE_MOTTATT_TIDSPUNKT_KEY, dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getDokumentTypeId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY, dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getDokumentKategori().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.DOKUMENTKATEGORI_ID_KEY, dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getPayloadAsString().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, "payload", dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getHarTema()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.TEMA_KEY, dataWrapper.getId()).toException();
        }
    }

    private void setFellesWrapperAttributter(MottakMeldingDataWrapper dataWrapper, Dokument dokument, DokumentMetadata metadata) {
        if(!dataWrapper.getHarTema()) {
            dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        }
        if (metadata != null) {
            dataWrapper.setAktørId(metadata.getBrukerId());
            metadata.getArkivId().ifPresent(dataWrapper::setArkivId);
            metadata.getSaksnummer().ifPresent(dataWrapper::setSaksnummer);
        }
        if (dokument != null) {
            dataWrapper.setDokumentTypeId(dokument.getDokumentTypeId());
            dataWrapper.setDokumentKategori(utledDokumentKategori(dokument));
            dataWrapper.setPayload(dokument.getKlartekstDokument());
            kopierOgValiderAttributterFraSøknad(dataWrapper, dokument);
        }
        if (!dataWrapper.getForsendelseMottattTidspunkt().isPresent()) {
            dataWrapper.setForsendelseMottattTidspunkt(FPDateUtil.nå());
        }
    }

    private void setFellesWrapperAttributterFraFagsak(MottakMeldingDataWrapper dataWrapper, FagsakInfomasjonDto fagsakInfo, Optional<Dokument> dokumentInput) {
        BehandlingTema behandlingTemaFraSak = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, fagsakInfo.getBehandlingstemaOffisiellKode(), BehandlingTema.UDEFINERT);

        if (dokumentInput.isPresent()) {
            Dokument dokument = dokumentInput.get();
            if (!fagsakInfo.getAktørId().equals(dataWrapper.getAktørId().orElse(null))) {
                throw BehandleDokumentforsendelseFeil.FACTORY.aktørIdMismatch().toException();
            }
            sjekkForMismatchMellomFagsakOgDokumentInn(dataWrapper, behandlingTemaFraSak, dokument);
        } else {
            UUID forsendelseId = dataWrapper.getForsendelseId().get(); // NOSONAR
            Dokument dokument = dokumentRepository.hentDokumenter(forsendelseId).stream().findFirst().get();
            dataWrapper.setDokumentTypeId(dokument.getDokumentTypeId());
            dataWrapper.setDokumentKategori(utledDokumentKategori(dokument));
            dataWrapper.setAktørId(fagsakInfo.getAktørId());
            dataWrapper.setBehandlingTema(behandlingTemaFraSak);
            dataWrapper.setForsendelseMottattTidspunkt(FPDateUtil.nå());
        }

    }

    private void sjekkForMismatchMellomFagsakOgDokumentInn(MottakMeldingDataWrapper dataWrapper, BehandlingTema fagsakTema, Dokument dokument) {

        if (DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.equals(dokument.getDokumentTypeId())) {
            // Endringssøknad har ingen info om behandlingstema, slik vi kan ikke utlede
            // et spesifikt tema, så må ha løsere match. Se HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType
            if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema())) {
                return;
            }
        } else if (fagsakTema.equals(dataWrapper.getBehandlingTema())) {
            return;
        }

        throw BehandleDokumentforsendelseFeil.FACTORY.behandlingTemaMismatch(
                dataWrapper.getBehandlingTema().getKode(), fagsakTema.getKode()).toException();
    }

    private void kopierOgValiderAttributterFraSøknad(MottakMeldingDataWrapper nesteSteg, Dokument dokument) {
        String xml = dokument.getKlartekstDokument();
        MottattStrukturertDokument<?> abstractMDto = MeldingXmlParser.unmarshallXml(xml);

        abstractMDto.kopierTilMottakWrapper(nesteSteg, aktørConsumer::hentAktørIdForPersonIdent);
    }

    private DokumentKategori utledDokumentKategori(Dokument dokument) {
        // FIXME (Humle): midlertidig dokumentkategori utledning - mangler regler for hvor dette skal utledes
        DokumentTypeId dti = dokument.getDokumentTypeId();
        if (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON.equals(dti) || DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.equals(dti) ||
                DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON.equals(dti) || DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.equals(dti)) {
            return DokumentKategori.SØKNAD;
        } else if (DokumentTypeId.KLAGE_DOKUMENT.equals(dti)) {
            return DokumentKategori.KLAGE_ELLER_ANKE;
        } else {
            return DokumentKategori.UDEFINERT;
        }
    }

    /**
     * Startdato i mottatt søknaden er før fastsatt startdato for journalføring gjennom VL
     */
    private boolean sjekkOmSøknadenKreverManuellBehandling(final Optional<LocalDate> startDato) {
        if (startDato.isPresent()) {
            return startDato.get().isBefore(LocalDate.parse(konfigVerdiStartdatoForeldrepenger, DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            return true;
        }
    }
}
