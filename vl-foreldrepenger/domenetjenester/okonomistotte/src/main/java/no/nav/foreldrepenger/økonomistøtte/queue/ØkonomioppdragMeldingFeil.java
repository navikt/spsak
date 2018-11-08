package no.nav.foreldrepenger.økonomistøtte.queue;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ØkonomioppdragMeldingFeil extends DeklarerteFeil {
    ØkonomioppdragMeldingFeil FACTORY = FeilFactory.create(ØkonomioppdragMeldingFeil.class);

    @TekniskFeil(feilkode = "FP-744861", feilmelding = "Feil i parsing av oppdragskjema.oppdrag", logLevel = WARN)
    Feil uventetFeilVedProsesseringAvForsendelsesInfoXML(Exception cause);

    @TekniskFeil(feilkode = "FP-595437", feilmelding = "Uventet feil med JAXB ved parsing av melding oppdragskjema.oppdrag: %s", logLevel = WARN)
    Feil uventetFeilVedProsesseringAvForsendelsesInfoXMLMedJaxb(String msg, Exception cause);
}
