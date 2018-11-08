package no.nav.foreldrepenger.økonomistøtte.api;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ØkonomistøtteFeil extends DeklarerteFeil {
    ØkonomistøtteFeil FACTORY = FeilFactory.create(ØkonomistøtteFeil.class);

    @TekniskFeil(feilkode = "FP-588867", feilmelding = "Kan ikke konvertere dato %s til xmlformatert dato.", logLevel = LogLevel.ERROR)
    Feil datokonverteringsfeil(String dataId, DatatypeConfigurationException cause);

    @TekniskFeil(feilkode = "FP-536167", feilmelding = "Kan ikke konvertere oppdrag med id %s. Problemer ved generering av xml", logLevel = LogLevel.ERROR)
    Feil xmlgenereringsfeil(Long oppdragId, Exception cause);
}
