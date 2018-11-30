package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface RegisterdataInnhenterFeil extends DeklarerteFeil {
    RegisterdataInnhenterFeil FACTORY = FeilFactory.create(RegisterdataInnhenterFeil.class);



    @TekniskFeil(feilkode = "FP-005453",
            feilmelding = "Behandlingen kan ikke oppdateres. Mangler personopplysning for søker med aktørId %s",
            logLevel = LogLevel.WARN)
    Feil ingenPersonopplysningForEksisterendeBehandling(AktørId aktørID);
}
