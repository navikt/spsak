package no.nav.foreldrepenger.domene.vurderfagsystem.impl;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface VurderFagsystemFeil extends DeklarerteFeil {

    VurderFagsystemFeil FACTORY = FeilFactory.create(VurderFagsystemFeil.class);

    @TekniskFeil(feilkode = "FP-312374", feilmelding = "Kan ikke finne %s fra søknad i Vedtaksløsningen selv om annen part har sak i VL for samme barn", logLevel = LogLevel.ERROR)
    Feil brukersSaknummerIkkeFunnetIVLSelvOmAnnenPartsSakErDer(Saksnummer saksnummer);
}
