package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.sql.SQLException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface HåndtereDokumentFeil extends DeklarerteFeil {

    HåndtereDokumentFeil FACTORY = FeilFactory.create(HåndtereDokumentFeil.class);

    @TekniskFeil(feilkode = "FP-980324", feilmelding = "Fant ingen kompletthetssjekk for behandling av type %s", logLevel = WARN)
    Feil ukjentType(String fagsakYtelseType);

    @TekniskFeil(feilkode = "FP-879124", feilmelding = "Mer enn en implementasjon funnet av kompletthetssjekk for behandling av type %s", logLevel = WARN)
    Feil flereImplementasjonerAvKompletthetsSjekk(String fagsakYtelseType);

    @TekniskFeil(feilkode = "FP-842786",
        feilmelding = "Ugyldig payload - feil ved konverter fra XML til String.",
        logLevel = LogLevel.WARN)
    Feil feilVedKonverterFraXmlTilString(SQLException e);
}
