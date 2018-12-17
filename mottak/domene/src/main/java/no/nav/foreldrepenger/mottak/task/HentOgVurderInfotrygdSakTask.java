package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDate;
import java.time.Period;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdPersonIkkeFunnetException;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

/**
 * <p>ProssessTask som sjekker om det eksisterer en sak i InfoTrygd eller om søknaden er en klage eller anke.</p>
 */
@Dependent
@ProsessTask(HentOgVurderInfotrygdSakTask.TASKNAME)
public class HentOgVurderInfotrygdSakTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.hentOgVurderInfotrygdSak";

    private final TemporalAmount infotrygdSakGyldigPeriode;
    private final TemporalAmount infotrygdAnnenPartGyldigPeriode;
    private final TemporalAmount startdatoAkseptertDiff;

    private GsakSakTjeneste gsakSakTjeneste;
    private InfotrygdTjeneste infotrygdTjeneste;
    private AktørConsumerMedCache aktørConsumer;

    private static final Logger LOGGER = LoggerFactory.getLogger(HentOgVurderInfotrygdSakTask.class);

    @Inject
    public HentOgVurderInfotrygdSakTask(ProsessTaskRepository prosessTaskRepository, KodeverkRepository kodeverkRepository,
                                        GsakSakTjeneste gsakSakTjeneste, InfotrygdTjeneste infotrygdTjeneste, AktørConsumerMedCache aktørConsumer,
                                        @KonfigVerdi("infotrygd.sak.gyldig.periode") Instance<Period> sakPeriode,
                                        @KonfigVerdi("infotrygd.annen.part.gyldig.periode") Instance<Period> annenPartPeriode,
                                        @KonfigVerdi("infotrygd.inntektsmelding.startdato.akseptert.diff") Instance<Period> startdatoAkseptertDiff) {
        super(prosessTaskRepository, kodeverkRepository);

        this.gsakSakTjeneste = gsakSakTjeneste;
        this.infotrygdTjeneste = infotrygdTjeneste;
        this.aktørConsumer = aktørConsumer;

        this.infotrygdSakGyldigPeriode = sakPeriode.get();
        this.infotrygdAnnenPartGyldigPeriode = annenPartPeriode.get();
        this.startdatoAkseptertDiff = startdatoAkseptertDiff.get();
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        try {
            dataWrapper.getTema();
        } catch (IllegalStateException e) { // NOSONAR
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.TEMA_KEY, dataWrapper.getId()).toException();
        }
        if (DokumentTypeId.INNTEKTSMELDING.equals(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)) && !dataWrapper.getInntektsmeldingStartDato().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY, dataWrapper.getId()).toException();
        }
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema()) && !DokumentTypeId.INNTEKTSMELDING.equals(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))) {
            if (!dataWrapper.getAnnenPartId().isPresent()) {
                throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.ANNEN_PART_ID_KEY, dataWrapper.getId()).toException();
            }
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        Tema tema = dataWrapper.getTema();
        String fnr;
        TemporalAmount periode;

        if (DokumentTypeId.INNTEKTSMELDING.equals(dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))) {
            String fnrBruker = aktørConsumer.hentPersonIdentForAktørId(dataWrapper.getAktørId().get())
                    .orElseThrow(() -> MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException());

            LocalDate fom = FPDateUtil.iDag().minus(infotrygdSakGyldigPeriode);
            LocalDate gsakFom = fom.minus(infotrygdSakGyldigPeriode);
            List<GsakSak> sakerBruker = finnSaker(tema, fnrBruker, gsakFom);
            if (!sakerBruker.isEmpty() && erInfotrygdSakRelevantForInntektsmelding(fnrBruker, dataWrapper.getInntektsmeldingStartDato().get(), fom)) {
                return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            } else {
                return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
            }
        }

        if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema())) {
            fnr = aktørConsumer.hentPersonIdentForAktørId(dataWrapper.getAnnenPartId().get())
                    .orElseThrow(() -> MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException());
            periode = infotrygdAnnenPartGyldigPeriode;
            // Midlertidig kjønnstest for å unngå at løpende fedrekvoter gir manuell journalføring
            if (Character.digit(fnr.charAt(8), 10) % 2 != 0) {
                return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
            }
        } else if (BehandlingTema.gjelderEngangsstønad(dataWrapper.getBehandlingTema())) {
            return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
        } else {
            throw MottakMeldingFeil.FACTORY.ukjentBehandlingstema(dataWrapper.getBehandlingTema()).toException();
        }

        LocalDate fom = FPDateUtil.iDag().minus(periode);
        LocalDate gsakFom = fom.minus(periode);
        List<GsakSak> saker = finnSaker(tema, fnr, gsakFom);
        if (!saker.isEmpty() && erInfotrygdSakRelevant(fnr, dataWrapper, fom)) {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
        return dataWrapper.nesteSteg(OpprettSakTask.TASKNAME);
    }

    private List<GsakSak> finnSaker(Tema tema, String fnr, LocalDate gsakFom) {
        List<GsakSak> saker = gsakSakTjeneste.finnSaker(fnr);
        saker = saker.stream()
                .filter(infotrygdSak -> infotrygdSak.getFagsystem().equals(Fagsystem.INFOTRYGD))
                .filter(infotrygdSak -> infotrygdSak.getTema().equals(tema))
                .filter(infotrygdSak -> !infotrygdSak.getSistEndret().isPresent() || infotrygdSak.getSistEndret().get().isAfter(gsakFom))
                .collect(Collectors.toList());
        return saker;
    }

    private boolean erInfotrygdSakRelevant(String fnr, MottakMeldingDataWrapper dataWrapper, LocalDate fom) {

        if (BehandlingTema.gjelderEngangsstønad(dataWrapper.getBehandlingTema())) {
            return erInfotrygdSakRelevantForEngangsstønad(fnr, fom);
        } else if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema())) {
            return erInfotrygdSakRelevantForForeldrepenger(fnr, fom);
        } else {
            return false;
        }
    }

    /**
     * Sjekker om vi har en engangsstønad i infotrygd som er nyere enn 10 mnd(konfigurerbar verdi, men er pt satt til 10 mnd).
     *
     * @param fnr søkers fødselsnummer
     */
    private boolean erInfotrygdSakRelevantForEngangsstønad(String fnr, LocalDate fom) {
        List<InfotrygdSak> infotrygdSaker = hentInfotrygdSaker(fnr, fom);
        return infotrygdSaker.stream().anyMatch(InfotrygdSak::gjelderEngangsstonad);
    }

    /**
     * Sjekker om vi har en foreldrepenger i infotrygd som er nyere enn 18 mnd(konfigurerbar verdi, men er pt satt til 18 mnd).
     *
     * @param fnr søkers fødselsnummer
     */
    private boolean erInfotrygdSakRelevantForForeldrepenger(String fnr, LocalDate fom) {
        List<InfotrygdSak> infotrygdSaker = hentInfotrygdSaker(fnr, fom);
        return infotrygdSaker.stream().anyMatch(InfotrygdSak::gjelderForeldrepenger);
    }

    private boolean erInfotrygdSakRelevantForInntektsmelding(String fnr, LocalDate startDato, LocalDate fom) {
        List<InfotrygdSak> infotrygdSaker = hentInfotrygdSaker(fnr, fom);
        return infotrygdSaker.stream().filter(InfotrygdSak::gjelderForeldrepenger)
                .filter(infotrygdSak -> infotrygdSak.getIverksatt().isPresent())
                .map(infotrygdSak -> infotrygdSak.getIverksatt().get().isBefore(ChronoLocalDate.from(startDato.plus(startdatoAkseptertDiff))) &&
                        infotrygdSak.getIverksatt().get().isAfter(ChronoLocalDate.from(startDato.minus(startdatoAkseptertDiff))))
                .count() > 0;
    }

    private List<InfotrygdSak> hentInfotrygdSaker(String fnr, LocalDate fom) {
        try {
            return infotrygdTjeneste.finnSakListe(fnr, fom);
        } catch (InfotrygdPersonIkkeFunnetException e) {
            Feilene.FACTORY.feilFraInfotrygdSakFordeling(e).log(LOGGER);
        }
        return new ArrayList<>();
    }

    interface Feilene extends DeklarerteFeil {
        Feilene FACTORY = FeilFactory.create(Feilene.class);

        @TekniskFeil(feilkode = "FP-074122", feilmelding = "PersonIkkeFunnet fra infotrygdSak", logLevel = LogLevel.WARN)
        Feil feilFraInfotrygdSakFordeling(Exception cause);
    }
}
