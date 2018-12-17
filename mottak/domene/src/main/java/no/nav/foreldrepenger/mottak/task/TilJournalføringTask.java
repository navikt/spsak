package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>ProssessTask som utleder journalføringsbehov og forsøker rette opp disse.</p>
 */
@Dependent
@ProsessTask(TilJournalføringTask.TASKNAME)
public class TilJournalføringTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.tilJournalforing";


    private final TilJournalføringTjeneste journalføringTjeneste;
    private final EnhetsTjeneste enhetsidTjeneste;
    private final DokumentRepository dokumentRepository;
    private final AktørConsumerMedCache aktørConsumer;

    @Inject
    public TilJournalføringTask(ProsessTaskRepository prosessTaskRepository,
                                TilJournalføringTjeneste journalføringTjeneste,
                                EnhetsTjeneste enhetsidTjeneste,
                                KodeverkRepository kodeverkRepository,
                                DokumentRepository dokumentRepository,
                                AktørConsumerMedCache aktørConsumer) {
        super(prosessTaskRepository, kodeverkRepository);
        this.journalføringTjeneste = journalføringTjeneste;
        this.enhetsidTjeneste = enhetsidTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.aktørConsumer = aktørConsumer;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getSaksnummer().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
        }
    }

    @Transaction
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        Optional<String> fnr = aktørConsumer.hentPersonIdentForAktørId(dataWrapper.getAktørId().get());//NOSONAR
        if (!fnr.isPresent()) {
            throw MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException();
        }
        String enhetsId = enhetsidTjeneste.hentFordelingEnhetId(dataWrapper.getTema(), dataWrapper.getBehandlingTema(), dataWrapper.getJournalførendeEnhet(), fnr);
        if (dataWrapper.getArkivId() == null) {
            UUID forsendelseId = dataWrapper.getForsendelseId().orElseThrow(IllegalStateException::new);

            String arkivId = journalførDokumentforsendelse(forsendelseId, dataWrapper);
            dataWrapper.setArkivId(arkivId);
        } else {
            journalføringTjeneste.tilJournalføring(dataWrapper.getArkivId(), dataWrapper.getSaksnummer().get(), dataWrapper.getAktørId().get(), enhetsId);
        }
        Optional<UUID> forsendelseId = dataWrapper.getForsendelseId();
        if (forsendelseId.isPresent()) {
            dokumentRepository.oppdaterForsendelseMetadata(forsendelseId.get(), dataWrapper.getArkivId(), dataWrapper.getSaksnummer().get(), ForsendelseStatus.PENDING);
        }
        return dataWrapper.nesteSteg(KlargjorForVLTask.TASKNAME);
    }

    private String journalførDokumentforsendelse(UUID forsendelseId, MottakMeldingDataWrapper dataWrapper) {
        Boolean forsøkEndeligJF = true;

        DokumentforsendelseResponse response = journalføringTjeneste.journalførDokumentforsendelse(forsendelseId, dataWrapper.getSaksnummer(), dataWrapper.getAvsenderId(), forsøkEndeligJF, dataWrapper.getRetryingTask());

        return response.getJournalpostId();
    }
}
