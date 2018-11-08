package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring;

import static no.nav.vedtak.feil.LogLevel.ERROR;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface EndreUttakFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "FP-817091", feilmelding = "Fant ikke gjeldende periode for ny periode fom %s tom %s", logLevel = ERROR)
    Feil fantIkkeMatchendeGjeldendePeriode(LocalDate fom, LocalDate tom);

    @TekniskFeil(feilkode = "FP-811231", feilmelding = "Fant ikke gjeldende periode aktivitet for periode fom %s tom %s for arbeidsgiver %s - %s - %s", logLevel = ERROR)
    Feil fantIkkeMatchendeGjeldendePeriodeAktivitet(LocalDate fom, LocalDate tom, String arbeidsforholdId, String arbeidsforholdOrgnr, UttakArbeidType uttakArbeidType);
}
