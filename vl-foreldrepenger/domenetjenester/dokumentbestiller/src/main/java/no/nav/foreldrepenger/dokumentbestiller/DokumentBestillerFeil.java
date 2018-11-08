package no.nav.foreldrepenger.dokumentbestiller;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface DokumentBestillerFeil extends DeklarerteFeil {

    DokumentBestillerFeil FACTORY = FeilFactory.create(DokumentBestillerFeil.class);

    @TekniskFeil(feilkode = "FP-109013", feilmelding = "Fant ikke personinfo for aktørId: %s. Kan ikke bestille dokument", logLevel = LogLevel.WARN)
    Feil fantIkkeFnrForAktørId(AktørId aktørId);
    
    @TekniskFeil(feilkode = "FP-119013", feilmelding = "Fant ikke personinfo for aktørId: %s. Kan ikke bestille dokument", logLevel = LogLevel.WARN)
    Feil fantIkkeAdresse(AktørId aktørId);
}
