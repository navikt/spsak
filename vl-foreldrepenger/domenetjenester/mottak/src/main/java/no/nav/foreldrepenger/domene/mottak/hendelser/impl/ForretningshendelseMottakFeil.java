package no.nav.foreldrepenger.domene.mottak.hendelser.impl;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ForretningshendelseMottakFeil extends DeklarerteFeil {

    ForretningshendelseMottakFeil FEILFACTORY = FeilFactory.create(ForretningshendelseMottakFeil.class);

    @TekniskFeil(feilkode = "FP-524247", feilmelding = "Ukjent forretningshendelse '%s'", logLevel = LogLevel.WARN)
    Feil ukjentForretningshendelse(String forretningshendelseType);

    @TekniskFeil(feilkode = "FP-524248", feilmelding = "Det finnes fagsak for ytelsesbehandling, men ingen åpen eller innvilget ytelsesesbehandling Gjelder forretningshendelse '%s'", logLevel = LogLevel.WARN)
    Feil finnesYtelsebehandlingSomVerkenErÅpenEllerInnvilget(String forretningshendelseType);
}
