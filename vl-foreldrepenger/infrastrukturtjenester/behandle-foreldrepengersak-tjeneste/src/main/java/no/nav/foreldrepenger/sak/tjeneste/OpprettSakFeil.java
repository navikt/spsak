package no.nav.foreldrepenger.sak.tjeneste;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSakEksistererAlleredeException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSUgyldigInputException;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface OpprettSakFeil extends DeklarerteFeil {

    OpprettSakFeil FACTORY = FeilFactory.create(OpprettSakFeil.class);

    @TekniskFeil(feilkode = "FP-827920", feilmelding = "Finner ikke person med aktørID %s", logLevel = WARN, exceptionClass = UkjentPersonException.class)
    Feil finnerIkkePersonMedAktørId(AktørId aktørId);

    @TekniskFeil(feilkode = "FP-827921", feilmelding = "Finner ikke person aktørID ikke oppgitt", logLevel = WARN, exceptionClass = UkjentPersonException.class)
    Feil finnerIkkePersonAktørIdNull();

    @TekniskFeil(feilkode = "FP-106651", feilmelding = "Ukjent behandlingstemakode %s for aktørId %s", logLevel = WARN)
    Feil ukjentBehandlingstemaKode(String behandlingstemaKode, AktørId aktorId);

    @TekniskFeil(feilkode = "FP-514082", feilmelding = "Kan ikke opprette sak i GSAK fordi sak allerede eksisterer", logLevel = WARN, exceptionClass = SakEksistererAlleredeException.class)
    Feil kanIkkeOppretteIGsakFordiSakAlleredeEksisterer(WSSakEksistererAlleredeException cause);

    @TekniskFeil(feilkode = "FP-294905", feilmelding = "Kan ikke opprette sak i GSAK fordi input er ugyldig", logLevel = WARN)
    Feil kanIkkeOppretteIGsakFordiInputErUgyldig(WSUgyldigInputException cause);

    @TekniskFeil(feilkode = "FP-840572", feilmelding = "Finner ikke fagsak med angitt saksnummer %s", logLevel = WARN)
    Feil finnerIkkeFagsakMedSaksnummer(Saksnummer saksnummer);

    @TekniskFeil(feilkode = "FP-094919", feilmelding = "Saksnummer %s er ugyldig", logLevel = WARN)
    Feil ugyldigSaksnummer(Saksnummer saksnummer);

    @TekniskFeil(feilkode = "FP-863070", feilmelding = "Journalpost-Fagsak knytning finnes allerede. Journalpost '%s' er knyttet mot fagsak '%s'.", logLevel = WARN)
    Feil JournalpostAlleredeKnyttetTilAnnenFagsak(JournalpostId journalPostId, Long fagsakId);

    @TekniskFeil(feilkode = "FP-252259", feilmelding = "Fant fler saker i Gsak enn det listeoutputen klarer å returnere for spørring med Saksnummer '%s'.", logLevel = WARN)
    Feil finnSakForMangeForekomster(String saksnummer, FinnSakForMangeForekomster finnSakForMangeForekomster);

    @TekniskFeil(feilkode = "FP-755374", feilmelding = "Forventet unikt resultat for spørring i Gsak med Saksnummer '%s', men fikk %s resultater.", logLevel = WARN)
    Feil finnSakIkkeUniktResultat(String saksnummer, int antall);

    @TekniskFeil(feilkode = "FP-609471", feilmelding = "Kan ikke finne sak i GSAK fordi input er ugyldig", logLevel = WARN)
    Feil finnSakUgyldigInput(FinnSakUgyldigInput finnSakUgyldigInput);

    @TekniskFeil(feilkode = "FP-910638", feilmelding = "Fikk ikke opprettet sak i Gsak pga OpprettSakSakEksistererAllerede, men klarer ikke å finne igjen saken. Fagsak '%s'.", logLevel = WARN)
    Feil fantIkkeSakenSomGsakSaAlleredeEksisterer(Long fagsakId);

    @ManglerTilgangFeil(feilkode = "FP-605357", feilmelding = "Mangler tilgang til å utføre opprettSak mot Gsak", logLevel = ERROR)
    Feil opprettSakSikkerhetsbegrensning(WSSikkerhetsbegrensningException e);
}
