package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import javax.xml.datatype.DatatypeConfigurationException;

public interface SakOgBehandlingFeil extends DeklarerteFeil {
    SakOgBehandlingFeil FACTORY = FeilFactory.create(SakOgBehandlingFeil.class);

    @TekniskFeil(feilkode = "FP-501696", feilmelding = "Feil parsing av LocalDate til XmlGregorianCalendar", logLevel = LogLevel.ERROR)
    Feil xmlGregorianCalendarParsingFeil(DatatypeConfigurationException cause);
}
