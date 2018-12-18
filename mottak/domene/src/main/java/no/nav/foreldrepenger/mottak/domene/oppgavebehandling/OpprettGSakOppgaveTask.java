package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask.TASKNAME;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_MOTTATT_TIDSPUNKT_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.SAKSNUMMER_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BrukerType;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.FagomradeKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.OppgaveKodeType;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.PrioritetKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.util.FPDateUtil;

/**
 * <p>
 * ProsessTask som oppretter en oppgave i GSAK for manuell behandling av tilfeller som ikke
 * kan håndteres automatisk av vedtaksløsningen.
 * <p>
 * </p>
 */
@Dependent
@ProsessTask(TASKNAME)
public class OpprettGSakOppgaveTask implements ProsessTaskHandler {

    public static final String TASKNAME = "integrasjon.gsak.opprettOppgave";

    private static final FagomradeKode FAGOMRADE_KODE = FagomradeKode.FOR;
    private static final PrioritetKode PRIORITET_KODE = PrioritetKode.NORM_FOR;
    private static final boolean IKKE_LEST = false;
    private static final Logger log = LoggerFactory.getLogger(OpprettGSakOppgaveTask.class);
    public static final String JFR_OMS = "JFR_OMS";
    private BehandleoppgaveConsumer service;

    private EnhetsTjeneste enhetsidTjeneste;
    private KodeverkRepository kodeverkRepository;
    private DokumentRepository dokumentRepository;
    private TilJournalføringTjeneste tilJournalføringTjeneste;
    private AktørConsumerMedCache aktørConsumer;

    @Inject
    public OpprettGSakOppgaveTask(BehandleoppgaveConsumer service,
                                  EnhetsTjeneste enhetsidTjeneste,
                                  KodeverkRepository kodeverkRepository,
                                  DokumentRepository dokumentRepository,
                                  TilJournalføringTjeneste tilJournalføringTjeneste,
                                  AktørConsumerMedCache aktørConsumer) {
        this.service = service;
        this.enhetsidTjeneste = enhetsidTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.dokumentRepository = dokumentRepository;
        this.tilJournalføringTjeneste = tilJournalføringTjeneste;
        this.aktørConsumer = aktørConsumer;
    }

    @Timed
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        BehandlingTema behandlingTema = finnBehandlingTema(Optional.ofNullable(prosessTaskData.getPropertyValue(BEHANDLINGSTEMA_KEY)));
        DokumentTypeId dokumentTypeId = Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY))
                .map(dt -> kodeverkRepository.finn(DokumentTypeId.class, dt)).orElse(DokumentTypeId.UDEFINERT);
        Tema tema = Optional.ofNullable(prosessTaskData.getPropertyValue(TEMA_KEY))
                .map(dt -> kodeverkRepository.finn(Tema.class, dt)).orElse(Tema.UDEFINERT);
        boolean retrying = Optional.ofNullable(prosessTaskData.getPropertyValue(RETRY_KEY)).map("J"::equalsIgnoreCase).orElse(false);
        behandlingTema = kodeverkRepository.finn(BehandlingTema.class, HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(tema, behandlingTema, dokumentTypeId));

        WSOpprettOppgaveResponse oppgaveResponse = opprettOppgave(prosessTaskData, behandlingTema, dokumentTypeId, retrying);

        //metricRegistry.meter("mottak." + ( behandlingTema.getOffisiellKode() != null ? behandlingTema.getOffisiellKode() : "udefinert" ) + ".ny.gsak").mark();

        String oppgaveId = oppgaveResponse.getOppgaveId();
        log.info("Oppgave opprettet i Gosys med nummer: {}", oppgaveId);
    }

    private BehandlingTema finnBehandlingTema(Optional<String> kode) {
        BehandlingTema behandlingTema;
        try {
            behandlingTema = kode.map(k -> kodeverkRepository.finn(BehandlingTema.class, k)).orElse(BehandlingTema.UDEFINERT);
        } catch (NoResultException e) { //NOSONAR
            // Vi skal tåle ukjent behandlingstema
            behandlingTema = BehandlingTema.UDEFINERT;
        }
        return behandlingTema;
    }

    /**
     * Det er to typer oppgaver som kan sendes til GSAK. Journalføringsoppgave eller fordelingsopgpave.
     * Fordelingsoppgave er ikke lenger i bruk. EnhetsId til andre oppgaver skal hentes fra ekstern tjeneste.
     */
    private WSOpprettOppgaveResponse opprettOppgave(ProsessTaskData prosessTaskData, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, boolean retrying) {
        final Optional<String> fødselsnr = hentPersonidentifikatorFraTaskData(prosessTaskData.getAktørId());
        String forsendelseId = prosessTaskData.getPropertyValue(FORSENDELSE_ID_KEY);
        final String enhetInput = prosessTaskData.getPropertyValue(JOURNAL_ENHET);

        String arkivId;
        if (forsendelseId != null) {
            UUID forsendelseIdUUID = UUID.fromString(forsendelseId);
            arkivId = midlertidigJournalførDokumentforsendelse(prosessTaskData, forsendelseIdUUID, retrying);
        } else {
            arkivId = prosessTaskData.getPropertyValue(ARKIV_ID_KEY);
        }

        // Overstyr saker fra NFP+NK, deretter egen logikk hvis fødselsnummer ikke er oppgitt
        final String enhetId = enhetsidTjeneste.hentFordelingEnhetId(hentUtTema(prosessTaskData), behandlingTema, Optional.ofNullable(enhetInput), fødselsnr);
        final String beskrivelse = lagBeskrivelse(behandlingTema, dokumentTypeId);

        OpprettOppgaveRequest request = createRequest(prosessTaskData, enhetId, beskrivelse, behandlingTema, dokumentTypeId, arkivId);

        return service.opprettOppgave(request);

    }

    private String lagBeskrivelse(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        if (DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return BehandlingTema.UDEFINERT.equals(behandlingTema) ? "Journalføring" : behandlingTema.getNavn();
        }
        return dokumentTypeId.getNavn();
    }

    private OpprettOppgaveRequest createRequest(ProsessTaskInfo prosessTaskData, String enhetsId, String beskrivelse,
                                                BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, String arkivId) {
        OpprettOppgaveRequest.Builder builder = OpprettOppgaveRequest.builder();

        // Kodeverk fra FGSAK / Gosys. Søk etter ENGANGSST_FOR på confluence og bruk verdier fra regneark (sic)....
        setFagområdeOgPrioritet(prosessTaskData, builder, behandlingTema, dokumentTypeId);
        Optional<String> fnr = hentPersonidentifikatorFraTaskData(prosessTaskData.getAktørId());
        if (fnr.isPresent()) {
            builder = builder.medFnr(fnr.get());
        }
        if (prosessTaskData.getPropertyValue(SAKSNUMMER_KEY) != null) {
            builder.medSaksnummer(prosessTaskData.getPropertyValue(SAKSNUMMER_KEY));
        }

        return builder
                .medOpprettetAvEnhetId(Integer.parseInt(enhetsId))
                .medAnsvarligEnhetId(enhetsId)
                .medDokumentId(arkivId)
                .medBrukerTypeKode(BrukerType.PERSON)
                .medMottattDato(hentDatoFraTaskData(prosessTaskData.getPropertyValue(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY)))
                .medAktivFra(FPDateUtil.iDag())
                .medAktivTil(helgeJustertFrist(FPDateUtil.iDag().plusDays(1L)))
                .medBeskrivelse(beskrivelse)
                .medLest(IKKE_LEST)
                .build();
    }

    private void setFagområdeOgPrioritet(ProsessTaskInfo info, OpprettOppgaveRequest.Builder builder, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId) {
        Tema tema = hentUtTema(info);
        if (Tema.FORELDRE_OG_SVANGERSKAPSPENGER.equals(tema)) {
            builder.medFagomradeKode(FAGOMRADE_KODE.toString())
                    .medPrioritetKode(PRIORITET_KODE.toString())
                    .medOppgavetypeKode(OppgaveKodeType.JFR_FOR.toString());
            if (BehandlingTema.gjelderForeldrepenger(behandlingTema) || dokumentTypeId.erForeldrepengerRelatert()) {
                builder.medUnderkategoriKode("FORELDREPE_FOR");
            } else if (BehandlingTema.gjelderEngangsstønad(behandlingTema) || dokumentTypeId.erEngangsstønadRelatert()) {
                builder.medUnderkategoriKode("ENGANGSST_FOR");
            }
        } else if(Tema.OMS.equals(tema)) {
            builder.medFagomradeKode(FagomradeKode.OMS.getKode())
                    .medPrioritetKode(PrioritetKode.NORM_OMS.toString())
            .medOppgavetypeKode(JFR_OMS);
        }
    }

    private Tema hentUtTema(ProsessTaskInfo info) {
        return kodeverkRepository.finn(Tema.class, info.getProperties().getProperty(MottakMeldingDataWrapper.TEMA_KEY, "-"));
    }

    // Sett frist til mandag hvis fristen er i helgen.
    private LocalDate helgeJustertFrist(LocalDate dato) {
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            return dato.plusDays(1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue());
        }
        return dato;
    }

    private LocalDate hentDatoFraTaskData(String propertyValue) {
        return propertyValue == null ? null : LocalDateTime.parse(propertyValue).toLocalDate();
    }

    private Optional<String> hentPersonidentifikatorFraTaskData(String aktørId) {
        if (aktørId == null) {
            return Optional.empty();
        }
        return aktørConsumer.hentPersonIdentForAktørId(aktørId);
    }

    private String midlertidigJournalførDokumentforsendelse(ProsessTaskData prosessTaskData, UUID forsendelseId, boolean retrying) {
        Boolean forsøkEndeligJF = false;
        Optional<String> saksnummer = Optional.ofNullable(prosessTaskData.getPropertyValue(MottakMeldingDataWrapper.SAKSNUMMER_KEY));
        Optional<String> avsenderId = Optional.ofNullable(prosessTaskData.getPropertyValue(MottakMeldingDataWrapper.AVSENDER_ID_KEY));
        DokumentforsendelseResponse response = tilJournalføringTjeneste.journalførDokumentforsendelse(forsendelseId,
                saksnummer, avsenderId, forsøkEndeligJF, retrying);

        String arkivId = response.getJournalpostId();

        dokumentRepository.oppdaterForseldelseMedArkivId(forsendelseId, arkivId, ForsendelseStatus.GOSYS);

        return arkivId;
    }
}
