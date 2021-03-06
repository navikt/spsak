package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@Dependent
@ProsessTask(KlargjorForVLTask.TASKNAME)
public class KlargjorForVLTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.klargjoering";
    private KlargjørForVLTjeneste klargjørForVLTjeneste;
    private DokumentRepository dokumentRepository;

    @Inject
    public KlargjorForVLTask(ProsessTaskRepository prosessTaskRepository,
                             KodeverkRepository kodeverkRepository,
                             KlargjørForVLTjeneste klargjørForVLTjeneste,
                             DokumentRepository dokumentRepository) {
        super(prosessTaskRepository, kodeverkRepository);
        this.klargjørForVLTjeneste = klargjørForVLTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getSaksnummer().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getArkivId() == null) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.ARKIV_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        String xml = dataWrapper.getPayloadAsString().orElse(null);
        String saksnummer = dataWrapper.getSaksnummer().orElseThrow(() -> new IllegalStateException("Skulle allerede vært sjekket i precondition(...)"));
        String arkivId = dataWrapper.getArkivId();
        DokumentTypeId dokumenttypeId = dataWrapper.getDokumentTypeId().map(dtid -> kodeverkRepository.finn(DokumentTypeId.class, dtid)).orElse(DokumentTypeId.UDEFINERT);
        DokumentKategori dokumentKategori = dataWrapper.getDokumentKategori().map(kat -> kodeverkRepository.finn(DokumentKategori.class, kat)).orElse(DokumentKategori.UDEFINERT);
        String journalEnhet = dataWrapper.getJournalførendeEnhet().orElse(null);
        LocalDate forsendelseMottatt = dataWrapper.getForsendelseMottatt();
        BehandlingTema behandlingsTema = dataWrapper.getBehandlingTema();
        Optional<UUID> forsendelseId = dataWrapper.getForsendelseId();
        if(forsendelseId.isPresent()){
            dokumentRepository.oppdaterForsendelseMetadata(forsendelseId.get(), arkivId, saksnummer, ForsendelseStatus.FPSAK);
        }

        klargjørForVLTjeneste.klargjørForVL(xml, saksnummer, arkivId, dokumenttypeId, forsendelseMottatt, behandlingsTema, dataWrapper.getForsendelseId().orElse(null), dokumentKategori, journalEnhet);

        return null; // Siste steg, fpsak overtar nå
    }

}
