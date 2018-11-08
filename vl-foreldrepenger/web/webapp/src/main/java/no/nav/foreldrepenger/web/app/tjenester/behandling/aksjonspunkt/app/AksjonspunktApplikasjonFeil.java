package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.TomtResultatException;

public interface AksjonspunktApplikasjonFeil extends DeklarerteFeil {
    AksjonspunktApplikasjonFeil FACTORY = FeilFactory.create(AksjonspunktApplikasjonFeil.class);

    @TekniskFeil(feilkode = "FP-770743",
        feilmelding = "Finner ikke håndtering for aksjonspunkt med kode: %s", logLevel = WARN)
    Feil kanIkkeFinneAksjonspunktUtleder(String aksjonspunktKode);

    @TekniskFeil(feilkode = "FP-475766",
        feilmelding = "Finner ikke overstyringshåndterer for DTO: %s", logLevel = WARN)
    Feil kanIkkeFinneOverstyringshåndterer(String dtoNavn);

    @TekniskFeil(feilkode = "FP-475767",
        feilmelding = "Kan ikke utlede gjeldende fødselsdato ved bekreftelse av fødsel", logLevel = WARN, exceptionClass = TomtResultatException.class)
    Feil kanIkkeUtledeGjeldendeFødselsdato();

    @TekniskFeil(feilkode = "FP-605445",
        feilmelding = "Kan ikke aktivere aksjonspunkt med kode: %s", logLevel = LogLevel.ERROR)
    Feil kanIkkeAktivereAksjonspunkt(String aksjonspunktKode);
}
