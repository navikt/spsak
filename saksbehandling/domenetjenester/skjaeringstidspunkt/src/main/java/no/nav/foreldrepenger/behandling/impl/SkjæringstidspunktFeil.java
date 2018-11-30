package no.nav.foreldrepenger.behandling.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface SkjæringstidspunktFeil extends DeklarerteFeil {

    SkjæringstidspunktFeil FACTORY = FeilFactory.create(SkjæringstidspunktFeil.class);

    @TekniskFeil(feilkode = "FP-931232", feilmelding = "Finner ikke skjæringstidspunkt for foreldrepenger som forventet for behandling: %s", logLevel = LogLevel.WARN)
    Feil finnerIkkeSkjæringstidspunktForForeldrepenger(Behandling behandling);

    @TekniskFeil(feilkode = "FP-994124", feilmelding = "Ikke en foreldrepengersak: %s", logLevel = LogLevel.WARN)
    Feil ikkeForeldrepengerSak(Behandling behandling);

    @TekniskFeil(feilkode = "FP-783491", feilmelding = "Kan ikke utlede opplysningsperiode for %s", logLevel = LogLevel.WARN)
    Feil kanIkkeUtledeOpplysningsperiodeForBehandling(Behandling behandling);
}
