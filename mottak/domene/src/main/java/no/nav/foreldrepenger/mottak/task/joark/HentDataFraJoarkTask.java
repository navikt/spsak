package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste.erStrukturertDokument;
import static no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste.hentMetadataForStrukturertDokument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;
import no.nav.vedtak.util.StringUtils;

/**
 * <p>ProssessTask som håndterer uthenting av saksinformasjon fra Journalarkivet(joark).</p>
 * <p>Avhengig av integerasjonen mot Journalarkivet for uthenting av metadata og søknads-xml.</p>
 */
@Dependent
@ProsessTask(HentDataFraJoarkTask.TASKNAME)
public class HentDataFraJoarkTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.hentFraJoark";

    private static final Logger log = LoggerFactory.getLogger(HentDataFraJoarkTask.class);

    private JoarkDokumentHåndterer joarkDokumentHåndterer;
    private String fastsattInntektsmeldingStartdatoFristForManuellBehandling;
    private AktørConsumer aktørConsumer;

    @Inject
    public HentDataFraJoarkTask(ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository, JoarkDokumentHåndterer joarkDokumentHåndterer,
                                @KonfigVerdi(value = "foreldrepenger.startdato") String fastsattInntektsmeldingStartdatoFristForManuellBehandling, AktørConsumer aktørConsumer) {
        super(prosessTaskRepository, kodeverkRepository);
        this.joarkDokumentHåndterer = joarkDokumentHåndterer;
        this.fastsattInntektsmeldingStartdatoFristForManuellBehandling = fastsattInntektsmeldingStartdatoFristForManuellBehandling;
        this.aktørConsumer = aktørConsumer;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (StringUtils.nullOrEmpty(dataWrapper.getArkivId())) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.ARKIV_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (!OpprettGSakOppgaveTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType()) && !dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        final List<JournalMetadata<DokumentTypeId>> hoveddokumenter = joarkDokumentHåndterer.hentJoarkDokumentMetadata(dataWrapper.getArkivId());
        if (hoveddokumenter.isEmpty()) {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        // Legg til task-parametere fra innkommende journalpost
        DokumentTypeId dokumentTypeId = HentDataFraJoarkTjeneste.hentDokumentTypeId(hoveddokumenter);
        dataWrapper.setDokumentTypeId(kodeverkRepository.finn(DokumentTypeId.class, dokumentTypeId));
        dataWrapper.setDokumentKategori(HentDataFraJoarkTjeneste.hentDokumentKategori(hoveddokumenter));
        dataWrapper.setBehandlingTema(kodeverkRepository.finn(BehandlingTema.class, HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(dataWrapper.getTema(), dataWrapper.getBehandlingTema(), dokumentTypeId)));
        dataWrapper.setForsendelseMottattTidspunkt(HentDataFraJoarkTjeneste.hentForsendelseMottattTidspunkt(hoveddokumenter));
        HentDataFraJoarkTjeneste.hentJournalførendeEnhet(hoveddokumenter).ifPresent(dataWrapper::setJournalførendeEnhet);
        joarkDokumentHåndterer.hentGyldigAktørFraMetadata(hoveddokumenter).ifPresent(dataWrapper::setAktørId);
        dataWrapper.setStrukturertDokument(erStrukturertDokument(hoveddokumenter));

        if (erStrukturertDokument(hoveddokumenter)) {
            JournalDokument<DokumentTypeId> journalDokument = joarkDokumentHåndterer.hentJournalDokument(hoveddokumenter);
            MottattStrukturertDokument<?> mottattDokument = joarkDokumentHåndterer.unmarshallXMLDokument(journalDokument.getXml());
            mottattDokument.kopierTilMottakWrapper(dataWrapper, joarkDokumentHåndterer::hentGyldigAktørFraPersonident);
            dataWrapper.setPayload(journalDokument.getXml());
            if (!(dataWrapper.getForsendelseMottattTidspunkt().isPresent())) {
                dataWrapper.setForsendelseMottattTidspunkt(FPDateUtil.nå());
            }
        }

        // Videre håndtering
        if (meldingGjelderForeldrePenger(dataWrapper.getBehandlingTema(), dokumentTypeId) && kanFordelesFP(dataWrapper)) {
            return håndterForeldrepengerRelatertDokument(dataWrapper, hoveddokumenter);
        }

        if (meldingGjelderEngangsstønad(dataWrapper.getBehandlingTema(), dokumentTypeId) && kanFordelesES(dataWrapper)) {
            return dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
        }

        return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);

    }

    private boolean meldingGjelderEngangsstønad(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        return BehandlingTema.gjelderEngangsstønad(behandlingTema) || dokumentTypeId.erEngangsstønadRelatert();
    }

    private boolean meldingGjelderForeldrePenger(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        return BehandlingTema.gjelderForeldrepenger(behandlingTema) || dokumentTypeId.erForeldrepengerRelatert();
    }

    private boolean kanFordelesES(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.erStrukturertDokument().orElse(false)) {
            return true;
        }
        BehandlingTema behandlingTema = dataWrapper.getBehandlingTema();
        return ((behandlingTema != null && BehandlingTema.gjelderEngangsstønad(behandlingTema))
                || dataWrapper.getDokumentTypeId().map(DokumentTypeId::erEngangsstønadRelatert).orElse(false))
                && dataWrapper.getAktørId().isPresent();
    }

    private boolean kanFordelesFP(MottakMeldingDataWrapper dataWrapper) {
        return dataWrapper.erStrukturertDokument().orElse(false);
    }

    private MottakMeldingDataWrapper håndterForeldrepengerRelatertDokument(MottakMeldingDataWrapper dataWrapper, List<JournalMetadata<DokumentTypeId>> hoveddokumenter) {
        JournalMetadata<DokumentTypeId> journalMetadata = hentMetadataForStrukturertDokument(hoveddokumenter);
        if (journalMetadata.getKanalReferanseId() != null) {
            log.info("Foreldrepenger-dokument mottat med kanalReferanseId: {}", journalMetadata.getKanalReferanseId());
        }

        // Ikke inntektsmelding
        if (!DokumentTypeId.INNTEKTSMELDING.equals(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))) {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        final LocalDate startDatoForForeldrepenger = dataWrapper.getInntektsmeldingStartDato().orElse(null);
        if (sjekkOmStartdatoForInntektsmeldingenKreverManuellBehandling(startDatoForForeldrepenger)) {
            log.info("Startdato for foreldrepenger er satt til {} som krever manuel journalføring.", startDatoForForeldrepenger);
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        if (sjekkOmInntektsmeldingGjelderMann(dataWrapper)) {
            log.info("Inntektsmelding gjelder far og krever manuell journalføring.", startDatoForForeldrepenger);
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        return dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
    }

    private boolean sjekkOmInntektsmeldingGjelderMann(MottakMeldingDataWrapper dataWrapper) {
        String aktørId = dataWrapper.getAktørId().orElseThrow(() -> new IllegalStateException("Utviklerfeil"));
        String fnrBruker = aktørConsumer.hentPersonIdentForAktørId(aktørId)
                .orElseThrow(() -> MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException());
        return Character.getNumericValue(fnrBruker.charAt(8)) % 2 != 0;
    }

    private boolean sjekkOmStartdatoForInntektsmeldingenKreverManuellBehandling(final LocalDate startDatoForForeldrepenger) {
        if (startDatoForForeldrepenger == null) {
            return true; // dersom startdato ikke er satt må meldingen behandles manuelt
        }
        return startDatoForForeldrepenger.isBefore(LocalDate.parse(fastsattInntektsmeldingStartdatoFristForManuellBehandling, DateTimeFormatter.ISO_LOCAL_DATE));
    }
}