package no.nav.foreldrepenger.jsonfeed;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface HendelsePublisererFeil extends DeklarerteFeil {

    HendelsePublisererFeil FACTORY = FeilFactory.create(HendelsePublisererFeil.class);

    @TekniskFeil(feilkode = "FP-184343", feilmelding = "Manglende originalBehandling for BehandlingVedtak om endring", logLevel = LogLevel.ERROR)
    Feil manglerOriginialBehandlingPåEndringsVedtak();

    @TekniskFeil(feilkode = "FP-343184", feilmelding = "Finner ikke noen relevant uttaksplan for vedtak", logLevel = LogLevel.ERROR)
    Feil finnerIkkeRelevantUttaksplanForVedtak();

    @TekniskFeil(feilkode = "FP-792048", feilmelding = "Ukjent type %s funnet for sekvensnummer %s i vedtak-json-feed", logLevel = LogLevel.WARN)
    Feil ukjentHendelseMeldingstype(String type, Long sekvensnummer);

    @TekniskFeil(feilkode = "FP-213891", feilmelding = "Finner ikke siste behandling for fagsak", logLevel = LogLevel.ERROR)
    Feil finnerIkkeBehandlingForFagsak();
}
