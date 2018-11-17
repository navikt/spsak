package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingAvsluttetEvent;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Oppgaveinfo;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.tjeneste.virksomhet.oppgave.v3.informasjon.oppgave.Oppgave;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeResponse;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BrukerType;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.FagomradeKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.FerdigstillOppgaveRequestMal;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.PrioritetKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.integrasjon.oppgave.FinnOppgaveListeFilterMal;
import no.nav.vedtak.felles.integrasjon.oppgave.FinnOppgaveListeRequestMal;
import no.nav.vedtak.felles.integrasjon.oppgave.FinnOppgaveListeSokMal;
import no.nav.vedtak.felles.integrasjon.oppgave.OppgaveConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class OppgaveTjenesteImpl implements OppgaveTjeneste {
    private static final int DEFAULT_OPPGAVEFRIST_DAGER = 1;
    private static final String DEFAULT_OPPGAVEBESKRIVELSE = "Må behandle sak i VL!";

    private static final String FORELDREPENGESAK_MÅ_FLYTTES_TIL_INFOTRYGD = "Foreldrepengesak må flyttes til Infotrygd";

    private static final String NØS_ANSVARLIG_ENHETID = "4151";
    private static final String NØS_FAGOMRÅDE_REGNSKAP_UTBETALING = "TSO";

    // Gosys' kodeverk. Søk på confluence etter ENGANGSST_FOR og se regneark v3.x.y
    private static final String FP_UNDERKATEGORI = "FORELDREPE_STO";
    private Logger logger = LoggerFactory.getLogger(OppgaveTjenesteImpl.class);
    private FagsakRepository fagsakRepository;

    private BehandlingRepository behandlingRepository;
    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private BehandleoppgaveConsumer service;
    private ProsessTaskRepository prosessTaskRepository;
    private TpsTjeneste tpsTjeneste;
    private OppgaveConsumer oppgaveConsumer;

    OppgaveTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public OppgaveTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                               OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository,
                               BehandleoppgaveConsumer service, OppgaveConsumer oppgaveConsumer,
                               ProsessTaskRepository prosessTaskRepository, TpsTjeneste tpsTjeneste) {
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.service = service;
        this.oppgaveConsumer = oppgaveConsumer;
        this.prosessTaskRepository = prosessTaskRepository;
        this.tpsTjeneste = tpsTjeneste;
    }

    @Override
    public String opprettBasertPåBehandlingId(Long behandlingId, OppgaveÅrsak oppgaveÅrsak) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return opprettOppgave(behandling, oppgaveÅrsak, DEFAULT_OPPGAVEBESKRIVELSE, PrioritetKode.NORM_FOR, DEFAULT_OPPGAVEFRIST_DAGER);
    }

    @Override
    public String opprettBehandleOppgaveForBehandling(Long behandlingId) {
        return opprettBehandleOppgaveForBehandlingMedPrioritetOgFrist(behandlingId, DEFAULT_OPPGAVEBESKRIVELSE, false, DEFAULT_OPPGAVEFRIST_DAGER);
    }

    @Override
    public String opprettBehandleOppgaveForBehandlingMedPrioritetOgFrist(Long behandlingId, String beskrivelse, boolean høyPrioritet, int fristDager) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        OppgaveÅrsak oppgaveÅrsak = behandling.getBehandleOppgaveÅrsak();
        return opprettOppgave(behandling, oppgaveÅrsak, beskrivelse, hentPrioritetKode(høyPrioritet), fristDager);
    }

    private String opprettOppgave(Behandling behandling, OppgaveÅrsak oppgaveÅrsak, String beskrivelse, PrioritetKode prioritetKode, int fristDager) {
        List<OppgaveBehandlingKobling> oppgaveBehandlingKoblinger = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        if (OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(oppgaveÅrsak, oppgaveBehandlingKoblinger).isPresent()) {
            // skal ikke opprette oppgave med samme årsak når behandlingen allerede har en åpen oppgave med den årsaken knyttet til seg
            return null;
        }
        Fagsak fagsak = behandling.getFagsak();
        Personinfo personSomBehandles = hentPersonInfo(behandling.getAktørId());
        OpprettOppgaveRequest request = createRequest(fagsak, personSomBehandles, oppgaveÅrsak, behandling.getBehandlendeEnhet(),
            beskrivelse, prioritetKode, fristDager);

        WSOpprettOppgaveResponse response = service.opprettOppgave(request);
        return behandleRespons(behandling, oppgaveÅrsak, response, fagsak.getSaksnummer());
    }

    /**
     * Observer endringer i BehandlingStatus og håndter oppgaver deretter.
     */
    public void observerBehandlingStatus(@Observes BehandlingAvsluttetEvent statusEvent) {
        Long behandlingId = statusEvent.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        opprettTaskAvsluttOppgave(behandling);
    }

    @Override
    public String opprettMedPrioritetOgBeskrivelseBasertPåFagsakId(Long fagsakId, OppgaveÅrsak oppgaveÅrsak, String enhetsId, String beskrivelse, boolean høyPrioritet) {
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(fagsakId);
        Personinfo personSomBehandles = hentPersonInfo(fagsak.getAktørId());
        OpprettOppgaveRequest request = createRequest(fagsak, personSomBehandles, oppgaveÅrsak, enhetsId, beskrivelse,
            hentPrioritetKode(høyPrioritet), DEFAULT_OPPGAVEFRIST_DAGER);
        WSOpprettOppgaveResponse response = service.opprettOppgave(request);
        return response.getOppgaveId();
    }

    @Override
    public List<OppgaveBehandlingKobling> hentOppgaverRelatertTilBehandling(Behandling behandling) {
        return oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
    }

    @Override
    public String opprettOppgaveStopUtbetalingAvARENAYtelse(long behandlingId, LocalDate førsteUttaksdato) {
        final String BESKRIVELSE = "Samordning arenaytelse. Vedtak foreldrepenger fra %s";

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        OpprettOppgaveRequest request = OpprettOppgaveRequest.builder()
            .medBeskrivelse(String.format(BESKRIVELSE, førsteUttaksdato))
            .medOpprettetAvEnhetId(Integer.parseInt(behandling.getBehandlendeEnhet()))
            .medAnsvarligEnhetId(NØS_ANSVARLIG_ENHETID)
            .medFagomradeKode(NØS_FAGOMRÅDE_REGNSKAP_UTBETALING)
            .medOppgavetypeKode(OppgaveÅrsak.SETT_ARENA_UTBET_VENT.getKode())
            .medUnderkategoriKode(FP_UNDERKATEGORI)
            .medPrioritetKode(PrioritetKode.HOY_FOR.toString())
            .medLest(false)
            .medAktivFra(LocalDate.now(FPDateUtil.getOffset()))
            .medAktivTil(helgeJustertFrist(LocalDate.now(FPDateUtil.getOffset()).plusDays(DEFAULT_OPPGAVEFRIST_DAGER)))
            .medBrukerTypeKode(BrukerType.PERSON)
            .medFnr(hentPersonInfo(behandling.getNavBruker().getAktørId()).getPersonIdent().getIdent())
            .medSaksnummer(saksnummer.getVerdi())
            .build();

        WSOpprettOppgaveResponse response = service.opprettOppgave(request);
        return behandleRespons(behandling, OppgaveÅrsak.SETT_ARENA_UTBET_VENT, response, saksnummer);
    }

    @Override
    public String opprettOppgaveSakSkalTilInfotrygd(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Saksnummer saksnummer = behandling.getFagsak().getSaksnummer();
        OpprettOppgaveRequest request = OpprettOppgaveRequest.builder()
            .medOpprettetAvEnhetId(Integer.parseInt(behandling.getBehandlendeEnhet()))
            .medAnsvarligEnhetId(behandling.getBehandlendeEnhet())
            .medFagomradeKode(FagomradeKode.FOR.getKode())
            .medFnr(hentPersonInfo(behandling.getNavBruker().getAktørId()).getPersonIdent().getIdent())
            .medAktivFra(LocalDate.now(FPDateUtil.getOffset()))
            .medAktivTil(helgeJustertFrist(LocalDate.now(FPDateUtil.getOffset()).plusDays(DEFAULT_OPPGAVEFRIST_DAGER)))
            .medOppgavetypeKode(OppgaveÅrsak.BEHANDLE_SAK_INFOTRYGD.getKode())
            .medSaksnummer(saksnummer.getVerdi())
            .medPrioritetKode(PrioritetKode.NORM_FOR.toString())
            .medBeskrivelse(FORELDREPENGESAK_MÅ_FLYTTES_TIL_INFOTRYGD)
            .medLest(false)
            .build();

        WSOpprettOppgaveResponse response = service.opprettOppgave(request);
        return behandleRespons(behandling, OppgaveÅrsak.BEHANDLE_SAK_INFOTRYGD, response, saksnummer);
    }

    private PrioritetKode hentPrioritetKode(boolean høyPrioritet) {
        return høyPrioritet ? PrioritetKode.HOY_FOR : PrioritetKode.NORM_FOR;
    }

    @Override
    public void avslutt(Long behandlingId, OppgaveÅrsak oppgaveÅrsak) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        List<OppgaveBehandlingKobling> oppgaveBehandlingKoblinger = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandlingId);
        Optional<OppgaveBehandlingKobling> oppgave = OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(oppgaveÅrsak, oppgaveBehandlingKoblinger);
        if (oppgave.isPresent()) {
            avsluttOppgave(behandling, oppgave.get());
        } else {
            OppgaveFeilmeldinger.FACTORY.oppgaveMedÅrsakIkkeFunnet(oppgaveÅrsak.getNavn(), behandlingId).log(logger);
        }
    }

    @Override
    public void avslutt(Long behandlingId, String oppgaveId) {
        Optional<OppgaveBehandlingKobling> oppgave = oppgaveBehandlingKoblingRepository.hentOppgaveBehandlingKobling(oppgaveId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (oppgave.isPresent()) {
            avsluttOppgave(behandling, oppgave.get());
        } else {
            OppgaveFeilmeldinger.FACTORY.oppgaveMedIdIkkeFunnet(oppgaveId, behandlingId).log(logger);
        }
    }

    private void avsluttOppgave(Behandling behandling, OppgaveBehandlingKobling aktivOppgave) {
        if (!aktivOppgave.isFerdigstilt()) {
            ferdigstillOppgaveBehandlingKobling(aktivOppgave);
        }
        FerdigstillOppgaveRequestMal request = createFerdigstillRequest(behandling, aktivOppgave.getOppgaveId());
        service.ferdigstillOppgave(request);
    }


    private void ferdigstillOppgaveBehandlingKobling(OppgaveBehandlingKobling aktivOppgave) {
        aktivOppgave.ferdigstillOppgave(SubjectHandler.getSubjectHandler().getUid());
        oppgaveBehandlingKoblingRepository.lagre(aktivOppgave);
    }

    @Override
    public void avsluttOppgaveOgStartTask(Behandling behandling, OppgaveÅrsak oppgaveÅrsak, String taskType) {
        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        taskGruppe.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        opprettTaskAvsluttOppgave(behandling, oppgaveÅrsak, false).ifPresent(taskGruppe::addNesteSekvensiell);
        taskGruppe.addNesteSekvensiell(opprettProsessTask(behandling, taskType));

        taskGruppe.setCallIdFraEksisterende();

        prosessTaskRepository.lagre(taskGruppe);
    }

    @Override
    public List<Oppgaveinfo> hentOppgaveListe(AktørId aktørId, List<String> oppgaveÅrsaker) {
        PersonIdent personIdent = hentPersonInfo(aktørId).getPersonIdent();
        FinnOppgaveListeRequestMal.Builder requestMalBuilder = new FinnOppgaveListeRequestMal.Builder();
        FinnOppgaveListeSokMal sokMal = FinnOppgaveListeSokMal.builder().medBrukerId(personIdent.getIdent()).build();
        FinnOppgaveListeFilterMal filterMal = FinnOppgaveListeFilterMal.builder().medOppgavetypeKodeListe(oppgaveÅrsaker).build();
        FinnOppgaveListeRequestMal requestMal = requestMalBuilder.medSok(sokMal).medFilter(filterMal).build();
        FinnOppgaveListeResponse finnOppgaveListeResponse = oppgaveConsumer.finnOppgaveListe(requestMal);
        List<Oppgave> oppgaveListe = finnOppgaveListeResponse.getOppgaveListe();
        return oppgaveListe.stream().map(ol -> new Oppgaveinfo(ol.getOppgavetype().getKode(), ol.getStatus().getKode())).collect(Collectors.toList());
    }

    @Override
    public Optional<ProsessTaskData> opprettTaskAvsluttOppgave(Behandling behandling) {
        List<OppgaveBehandlingKobling> oppgaver = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        Optional<OppgaveBehandlingKobling> oppgave = oppgaver.stream().filter(kobling -> !kobling.isFerdigstilt()).findFirst();
        if (oppgave.isPresent()) {
            return opprettTaskAvsluttOppgave(behandling, oppgave.get().getOppgaveÅrsak());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProsessTaskData> opprettTaskAvsluttOppgave(Behandling behandling, OppgaveÅrsak oppgaveÅrsak) {
        return opprettTaskAvsluttOppgave(behandling, oppgaveÅrsak, true);
    }

    @Override
    public Optional<ProsessTaskData> opprettTaskAvsluttOppgave(Behandling behandling, OppgaveÅrsak oppgaveÅrsak, boolean skalLagres) {
        List<OppgaveBehandlingKobling> oppgaveBehandlingKoblinger = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        Optional<OppgaveBehandlingKobling> oppgave = OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(oppgaveÅrsak, oppgaveBehandlingKoblinger);
        if (oppgave.isPresent()) {
            OppgaveBehandlingKobling aktivOppgave = oppgave.get();
            // skal ikke avslutte oppgave av denne typen
            if (OppgaveÅrsak.BEHANDLE_SAK_INFOTRYGD.equals(aktivOppgave.getOppgaveÅrsak())) {
                return Optional.empty();
            }
            ferdigstillOppgaveBehandlingKobling(aktivOppgave);
            ProsessTaskData avsluttOppgaveTask = opprettProsessTask(behandling, AvsluttOppgaveTaskProperties.TASKTYPE);
            avsluttOppgaveTask.setOppgaveId(aktivOppgave.getOppgaveId());
            if (skalLagres) {
                avsluttOppgaveTask.setCallIdFraEksisterende();
                prosessTaskRepository.lagre(avsluttOppgaveTask);
            }
            return Optional.of(avsluttOppgaveTask);
        } else {
            return Optional.empty();
        }
    }

    private ProsessTaskData opprettProsessTask(Behandling behandling, String taskType) {
        ProsessTaskData prosessTask = new ProsessTaskData(taskType);
        prosessTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        return prosessTask;
    }

    private FerdigstillOppgaveRequestMal createFerdigstillRequest(Behandling behandling, String oppgaveId) {
        FerdigstillOppgaveRequestMal.Builder builder = FerdigstillOppgaveRequestMal.builder().medOppgaveId(oppgaveId);
        if (behandling.getBehandlendeEnhet() != null) {
            builder.medFerdigstiltAvEnhetId(Integer.parseInt(behandling.getBehandlendeEnhet()));
        }
        return builder.build();
    }

    private String behandleRespons(Behandling behandling, OppgaveÅrsak oppgaveÅrsak, WSOpprettOppgaveResponse response,
                                   Saksnummer saksnummer) {

        String oppgaveId = response.getOppgaveId();
        OppgaveBehandlingKobling oppgaveBehandlingKobling = new OppgaveBehandlingKobling(oppgaveÅrsak, oppgaveId, saksnummer, behandling);
        oppgaveBehandlingKoblingRepository.lagre(oppgaveBehandlingKobling);
        return oppgaveId;
    }

    private Personinfo hentPersonInfo(AktørId aktørId) {
        return tpsTjeneste.hentBrukerForAktør(aktørId)
            .orElseThrow(() -> OppgaveFeilmeldinger.FACTORY.identIkkeFunnet(aktørId).toException());
    }

    private OpprettOppgaveRequest createRequest(Fagsak fagsak, Personinfo personinfo, OppgaveÅrsak oppgaveÅrsak,
                                                String enhetsId, String beskrivelse, PrioritetKode prioritetKode,
                                                int fristDager) {

        OpprettOppgaveRequest.Builder builder = OpprettOppgaveRequest.builder();

        if (fagsak.getYtelseType().gjelderForeldrepenger()) {
            builder = builder.medUnderkategoriKode(FP_UNDERKATEGORI);
        }
        return builder
            .medOpprettetAvEnhetId(Integer.parseInt(enhetsId))
            .medAnsvarligEnhetId(enhetsId)
            .medFagomradeKode(FagomradeKode.FOR.getKode())
            .medFnr(personinfo.getPersonIdent().getIdent())
            .medBrukerTypeKode(BrukerType.PERSON)
            .medAktivFra(LocalDate.now(FPDateUtil.getOffset()))
            .medAktivTil(helgeJustertFrist(LocalDate.now(FPDateUtil.getOffset()).plusDays(fristDager)))
            .medOppgavetypeKode(oppgaveÅrsak.getKode())
            .medSaksnummer(fagsak.getSaksnummer() != null ? fagsak.getSaksnummer().getVerdi() : fagsak.getId().toString()) // Mer iht PK-38815
            .medPrioritetKode(prioritetKode.toString())
            .medBeskrivelse(beskrivelse)
            .medLest(false)
            .build();
    }

    // Sett frist til mandag hvis fristen er i helgen.
    private LocalDate helgeJustertFrist(LocalDate dato) {
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            return dato.plusDays(1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue());
        }
        return dato;
    }
}
