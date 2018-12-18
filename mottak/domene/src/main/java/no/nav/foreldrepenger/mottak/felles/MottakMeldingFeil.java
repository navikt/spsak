package no.nav.foreldrepenger.mottak.felles;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.UUID;

import javax.xml.bind.JAXBException;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface MottakMeldingFeil extends DeklarerteFeil {

    MottakMeldingFeil FACTORY = FeilFactory.create(MottakMeldingFeil.class);

    @TekniskFeil(feilkode = "FP-420365", feilmelding = "Feil i parsing av dokumentnotifikasjon.v1.Forsendelsesinformasjon", logLevel = WARN)
    Feil uventetFeilVedProsesseringAvForsendelsesInfoXML(Exception cause);

    @TekniskFeil(feilkode = "FP-548969", feilmelding = "Uventet feil med JAXB ved parsing av melding dokumentnotifikasjon.v1.Forsendelsesinformasjon", logLevel = WARN)
    Feil uventetFeilVedProsesseringAvForsendelsesInfoXMLMedJaxb(JAXBException cause);

    @TekniskFeil(feilkode = "FP-941984", feilmelding = "Prosessering av preconditions for %s mangler %s. TaskId: %s", logLevel = WARN)
    Feil prosesstaskPreconditionManglerProperty(String taskname, String property, Long taskId);

    @TekniskFeil(feilkode = "FP-638068", feilmelding = "Prosessering av postconditions for %s mangler %s. TaskId: %s", logLevel = WARN)
    Feil prosesstaskPostconditionManglerProperty(String taskname, String property, Long taskId);

    @IntegrasjonFeil(feilkode = "FP-432607", feilmelding = "Kan ikke rette opp journalføringsmangler for JournalpostId: %s", logLevel = WARN, exceptionClass = KanIkkeFerdigstilleJournalFøringException.class)
    Feil kanIkkeRetteOppJournalmangler(String arkivId);

    @IntegrasjonFeil(feilkode = "FP-331190", feilmelding = "Fant ikke journal dokument", logLevel = LogLevel.WARN)
    Feil hentDokumentIkkeFunnet();

    @IntegrasjonFeil(feilkode = "FP-254631", feilmelding = "Fant ikke personident for aktørId i task %s.  TaskId: %s", logLevel = WARN)
    Feil fantIkkePersonidentForAktørId(String taskname, Long taskId);

    @IntegrasjonFeil(feilkode = "FP-254634", feilmelding = "Fant ikke aktørId for personident i task %s. TaskId: %s", logLevel = WARN)
    Feil fantIkkeAktørIdForPersonident(String taskname, Long taskId);

    @TekniskFeil(feilkode = "FP-404782", feilmelding = "Ulik behandlingstemakode i tynnmelding (%s) og søknadsdokument (%s)", logLevel = ERROR)
    Feil ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument(String behandlingstemaKodeTynnmelding, String behandlingstemaKodeSøknadsdokument);

    @TekniskFeil(feilkode = "FP-401245", feilmelding = "Ulikt saksnummer i tynnmelding (%s) og søknadsdokument (%s)", logLevel = WARN)
    Feil ulikSaksnummerITynnmeldingOgSøknadsdokument(String saksnummerTynnmelding, String saksnummerSøknadsdokument);

    @TekniskFeil(feilkode = "FP-502574", feilmelding = "Ulik aktørId i tynnmelding og søknadsdokument", logLevel = WARN)
    Feil ulikAktørIdITynnMeldingOgSøknadsdokument();

    @TekniskFeil(feilkode = "FP-513574", feilmelding = "Flere enn en fødselsdato i fødselssøknad", logLevel = WARN)
    Feil merEnnEnFødselsdatoPåFødselsøknad();

    @TekniskFeil(feilkode = "FP-785833", feilmelding = "Feil journaltilstand. Forventet tilstand: endelig, fikk: {%s}", logLevel = WARN)
    Feil feilJournalTilstandForventetTilstandEndelig(JournalTilstand journalTilstand);

    @TekniskFeil(feilkode = "FP-678125", feilmelding = "Mangler saksnummer for forsendelse [%s], nødvendig for journalføring", logLevel = WARN)
    Feil manglerSaksnummerForJournalføring(UUID forsendelseId);

    @TekniskFeil(feilkode = "FP-286143", feilmelding = "Ukjent behandlingstema {%s}", logLevel = WARN)
    Feil ukjentBehandlingstema(BehandlingTema behandlingTema);
}
