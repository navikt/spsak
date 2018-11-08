package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface SendVedtaksbrevFeil extends DeklarerteFeil {
    SendVedtaksbrevFeil FACTORY = FeilFactory.create(SendVedtaksbrevFeil.class);

    @TekniskFeil(feilkode = "FP-471756", feilmelding = "Kan ikke sende klagebrev(%s) for foreldrepenger ennå, i behandling: %s", logLevel = LogLevel.WARN)
    Feil kanIkkeSendeVedtaksbrev(String vedtakResultatType, Long behandlingId);
}
