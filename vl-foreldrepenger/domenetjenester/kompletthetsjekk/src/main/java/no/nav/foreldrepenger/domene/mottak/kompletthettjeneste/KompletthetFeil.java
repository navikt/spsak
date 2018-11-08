package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KompletthetFeil extends DeklarerteFeil {

    KompletthetFeil FACTORY = FeilFactory.create(KompletthetFeil.class);

    @TekniskFeil(feilkode = "FP-912913", feilmelding = "Mer enn en implementasjon funnet av KompletthetssjekkerSøknad for fagsakYtelseType=%s og behandlingType=%s", logLevel = WARN)
    Feil flereImplementasjonerAvKompletthetssjekkerSøknad(String fagsakYtelseType, String behandlingType);

    @TekniskFeil(feilkode = "FP-912912", feilmelding = "Fant ingen implementasjon av KompletthetssjekkerSøknad for fagsakYtelseType=%s og behandlingType=%s", logLevel = WARN)
    Feil ingenImplementasjonerAvKompletthetssjekkerSøknad(String fagsakYtelseType, String behandlingType);

    @TekniskFeil(feilkode = "FP-912911", feilmelding = "Mer enn en implementasjon funnet av Kompletthetsjekker for fagsakYtelseType=%s og behandlingType=%s", logLevel = WARN)
    Feil flereImplementasjonerAvKompletthetsjekker(String fagsakYtelseType, String behandlingType);

    @TekniskFeil(feilkode = "FP-912910", feilmelding = "Fant ingen implementasjon av Kompletthetsjekker for fagsakYtelseType=%s og behandlingType=%s", logLevel = WARN)
    Feil ingenImplementasjonerAvKompletthetssjekker(String fagsakYtelseType, String behandlingType);

    @TekniskFeil(feilkode = "FP-918661", feilmelding = "Fant ikke noe dokument for behandlingId = %s", logLevel = LogLevel.ERROR)
    Feil fantIkkeDokument(long id);
}
