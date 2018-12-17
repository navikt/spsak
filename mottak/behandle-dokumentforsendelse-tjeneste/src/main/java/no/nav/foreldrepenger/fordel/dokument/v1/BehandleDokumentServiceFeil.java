package no.nav.foreldrepenger.fordel.dokument.v1;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BehandleDokumentServiceFeil extends DeklarerteFeil {

    BehandleDokumentServiceFeil FACTORY = FeilFactory.create(BehandleDokumentServiceFeil.class);

    @TekniskFeil(feilkode = "FP-963070", feilmelding = "finner ikke fagsak for saksnummer: %s", logLevel = LogLevel.WARN)
    Feil finnerIkkeFagsak(String saksnummer);
}
