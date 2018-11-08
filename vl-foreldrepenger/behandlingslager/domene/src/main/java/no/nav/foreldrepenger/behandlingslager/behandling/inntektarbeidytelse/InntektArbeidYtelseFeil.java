package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface InntektArbeidYtelseFeil extends DeklarerteFeil {

    InntektArbeidYtelseFeil FACTORY = FeilFactory.create(InntektArbeidYtelseFeil.class);

    @TekniskFeil(feilkode = "FP-731232", feilmelding = "Finner ikke InntektArbeidYtelse grunnlag for behandling med id %s", logLevel = LogLevel.WARN)
    Feil fantIkkeForventetGrunnlagPåBehandling(long behandlingId);

    @TekniskFeil(feilkode = "FP-512369", feilmelding = "Aggregat kan ikke være null ved opprettelse av builder", logLevel = LogLevel.WARN)
    Feil aggregatKanIkkeVæreNull();

    @TekniskFeil(feilkode = "FP-765683", feilmelding = "Ukjent versjonstype.", logLevel = LogLevel.WARN)
    Feil ukjentVersjonstype();

}
