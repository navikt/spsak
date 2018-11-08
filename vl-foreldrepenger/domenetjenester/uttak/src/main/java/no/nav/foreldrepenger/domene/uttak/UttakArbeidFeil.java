package no.nav.foreldrepenger.domene.uttak;


import static no.nav.vedtak.feil.LogLevel.ERROR;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface UttakArbeidFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "FP-814321", feilmelding = "Fant ikke yrkesaktiviteter", logLevel = ERROR)
    Feil manglendeYrkesAktiviteter();

    @TekniskFeil(feilkode = "FP-677743", feilmelding = "Fant ikke beregningsgrunnlag for behandling %s", logLevel = ERROR)
    Feil manglendeBeregningsgrunnlag(Behandling behandling);
}
