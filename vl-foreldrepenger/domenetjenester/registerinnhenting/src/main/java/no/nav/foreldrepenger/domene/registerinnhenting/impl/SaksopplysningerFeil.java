package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface SaksopplysningerFeil extends DeklarerteFeil {
    SaksopplysningerFeil FACTORY = FeilFactory.create(SaksopplysningerFeil.class);

    @TekniskFeil(feilkode = "FP-258917", feilmelding = "Bruker %s: Finner ikke bruker i TPS", logLevel = LogLevel.WARN)
    Feil feilVedOppslagITPS(String ident);

}
