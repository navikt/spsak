package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ManuellRegistreringFeil extends DeklarerteFeil {
    ManuellRegistreringFeil FACTORY = FeilFactory.create(ManuellRegistreringFeil.class);

    @TekniskFeil(feilkode = "FP-703669", feilmelding = "Feil ved parsing av LocalDate til XmlGregorianCalendar", logLevel = LogLevel.ERROR)
    Feil xmlGregorianCalendarParsingFeil(DatatypeConfigurationException cause);

    @TekniskFeil(feilkode = "FP-453254", feilmelding = "Feil ved marshalling av søknadsskjema", logLevel = LogLevel.ERROR)
    Feil marshallingFeil(Exception cause);

    @TekniskFeil(feilkode = "FP-453257", feilmelding = "Fant ikke aktør-ID for fødselsnummer: %s.", logLevel = LogLevel.ERROR)
    Feil feilVedhentingAvAktørId(String fnr);

}
