package no.nav.foreldrepenger.domene.person.impl;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface TpsFeilmeldinger extends DeklarerteFeil {

    TpsFeilmeldinger FACTORY = FeilFactory.create(TpsFeilmeldinger.class);

    @TekniskFeil(feilkode = "FP-164686", feilmelding = "Person er ikke Bruker, kan ikke hente ut brukerinformasjon", logLevel = LogLevel.WARN)
    Feil ukjentBrukerType();

    @ManglerTilgangFeil(feilkode = "FP-432142", feilmelding = "TPS ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = ERROR)
    Feil tpsUtilgjengeligSikkerhetsbegrensning(HentPersonSikkerhetsbegrensning cause);

    @ManglerTilgangFeil(feilkode = "FP-432144", feilmelding = "TPS ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = ERROR)
    Feil tpsUtilgjengeligSikkerhetsbegrensning(HentPersonhistorikkSikkerhetsbegrensning cause);

    @TekniskFeil(feilkode = "FP-432143", feilmelding = "TPS ikke tilgjengelig, hentet ident fra cache", logLevel = WARN)
    Feil tpsUtilgjengeligHentetIdentFraCache();

    @TekniskFeil(feilkode = "FP-715013", feilmelding = "Fant ikke person i TPS", logLevel = WARN)
    Feil fantIkkePerson(HentPersonPersonIkkeFunnet cause);

    @TekniskFeil(feilkode = "FP-065124", feilmelding = "Fant ikke person i TPS", logLevel = WARN)
    Feil fantIkkePersonForFnr();

    @TekniskFeil(feilkode = "FP-065125", feilmelding = "Fant ikke personhistorikk i TPS", logLevel = WARN)
    Feil fantIkkePersonhistorikkForAktørId(HentPersonhistorikkPersonIkkeFunnet e);

    @TekniskFeil(feilkode = "FP-181235", feilmelding = "Fant ikke aktørId i TPS", logLevel = WARN)
    Feil fantIkkePersonForAktørId();

    @ManglerTilgangFeil(feilkode = "FP-115180", feilmelding = "TPS ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = ERROR)
    Feil tpsUtilgjengeligGeografiskTilknytningSikkerhetsbegrensing(HentGeografiskTilknytningSikkerhetsbegrensing cause);

    @TekniskFeil(feilkode = "FP-349049", feilmelding = "Fant ikke geografisk informasjon for person", logLevel = WARN)
    Feil geografiskTilknytningIkkeFunnet(HentGeografiskTilknytningPersonIkkeFunnet cause);

    @TekniskFeil(feilkode = "FP-349059", feilmelding = "Feil parsing av LocalDate til XmlGregorianCalendar", logLevel = LogLevel.ERROR)
    Feil xmlGregorianCalendarParsingFeil(DatatypeConfigurationException e);
}
