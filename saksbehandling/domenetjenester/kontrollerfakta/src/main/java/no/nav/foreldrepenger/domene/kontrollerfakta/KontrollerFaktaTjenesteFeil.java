package no.nav.foreldrepenger.domene.kontrollerfakta;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KontrollerFaktaTjenesteFeil extends DeklarerteFeil {

    KontrollerFaktaTjenesteFeil FACTORY = FeilFactory.create(KontrollerFaktaTjenesteFeil.class);

    @TekniskFeil(feilkode = "FP-995962", feilmelding = "Mer enn en implementasjon funnet av KontrollerFaktaTjeneste for fagsakYtelseType=%s og behandlingType=%s", logLevel = WARN)
    Feil flereImplementasjonerAvKontrollerFaktaTjeneste(String fagsakYtelseType, String behandlingType);

    @TekniskFeil(feilkode = "FP-770839", feilmelding = "Fant ingen implementasjon av KontrollerFaktaTjeneste for fagsakYtelseType=%s og behandlingType=%s", logLevel = WARN)
    Feil ingenImplementasjonAvKontrollerFaktaTjeneste(String fagsakYtelseType, String behandlingType);
}
