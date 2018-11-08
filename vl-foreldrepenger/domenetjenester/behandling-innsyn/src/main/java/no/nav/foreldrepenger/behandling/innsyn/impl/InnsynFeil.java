package no.nav.foreldrepenger.behandling.innsyn.impl;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InnsynFeil extends DeklarerteFeil {
    InnsynFeil FACTORY = FeilFactory.create(InnsynFeil.class);

    @TekniskFeil(feilkode = "FP-148968", feilmelding = "Finner ingen fagsak som kan gis innsyn for saksnummer: %s", logLevel = LogLevel.WARN)
    Feil tjenesteFinnerIkkeFagsakForInnsyn(Saksnummer saksnummer);
}
