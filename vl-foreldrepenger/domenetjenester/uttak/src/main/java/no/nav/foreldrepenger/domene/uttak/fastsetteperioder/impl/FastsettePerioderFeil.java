package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static no.nav.vedtak.feil.LogLevel.ERROR;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriodeAktivitet;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface FastsettePerioderFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "FP-818121", feilmelding = "Fant ikke opprinnelig periode for ny periode %s", logLevel = ERROR)
    Feil manglendeOpprinneligPeriode(UttakResultatPeriode periode);

    @TekniskFeil(feilkode = "FP-299466", feilmelding = "Finner ingen aktivitet i opprinnelig for ny aktivitet %s %s", logLevel = ERROR)
    Feil manglendeOpprinneligAktivitet(UttakResultatPeriodeAktivitet nyAktivitet, List<UttakResultatPeriodeAktivitetEntitet> aktiviteter);

    @TekniskFeil(feilkode = "FP-298156", feilmelding = "Må ha stillingsprosent på periode %s %s %s %s", logLevel = ERROR)
    Feil manglendeStillingsprosent(String orgnr, String arbeidsforholdRef, LocalDate dato, UttakArbeidType arbeidType);

    @TekniskFeil(feilkode = "FP-298777", feilmelding = "Støtter ikke permisjon i flere arbeidsforhold for samme virksomhet. Kommer senere..", logLevel = ERROR)
    Feil støtterIkkeFlereArbeidsforholdMedPerimisjonISammeVirksomhet();

    @TekniskFeil(feilkode = "FP-212347", feilmelding = "Finner ikke avklarte datoer for revurdering", logLevel = ERROR)
    Feil manglendeAvklarteDatoer();
}
