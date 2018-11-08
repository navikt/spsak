package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.feil.FeilFactory;

public class EndreUttakUtil {

    private EndreUttakUtil() {
    }

    public static UttakResultatPeriodeEntitet finnGjeldendePeriodeFor(UttakResultatPerioderEntitet gjeldende,
                                                               LocalDateInterval nyPeriode) {
        for (UttakResultatPeriodeEntitet gjeldendePeriode : gjeldende.getPerioder()) {
            if (new LocalDateInterval(gjeldendePeriode.getFom(), gjeldendePeriode.getTom()).contains(nyPeriode)) {
                return gjeldendePeriode;
            }
        }
        throw FeilFactory.create(EndreUttakFeil.class).fantIkkeMatchendeGjeldendePeriode(nyPeriode.getFomDato(),
            nyPeriode.getTomDato()).toException();
    }

    public static UttakResultatPeriodeAktivitetEntitet finnGjeldendeAktivitetFor(UttakResultatPeriodeEntitet gjeldendePeriode,
                                                                                 String arbeidsforholdId,
                                                                                 String arbeidsforholdOrgnr,
                                                                                 UttakArbeidType uttakArbeidType) {

        for (UttakResultatPeriodeAktivitetEntitet aktivitet : gjeldendePeriode.getAktiviteter()) {
            if (Objects.equals(aktivitet.getArbeidsforholdId(), arbeidsforholdId) &&
                Objects.equals(aktivitet.getArbeidsforholdOrgnr(), arbeidsforholdOrgnr) &&
                Objects.equals(aktivitet.getUttakArbeidType(), uttakArbeidType)) {
                return aktivitet;
            }
        }
        throw FeilFactory.create(EndreUttakFeil.class).fantIkkeMatchendeGjeldendePeriodeAktivitet(gjeldendePeriode.getFom(),
            gjeldendePeriode.getTom(), arbeidsforholdId, arbeidsforholdOrgnr, uttakArbeidType).toException();
    }

    public static UttakResultatPeriodeAktivitetEntitet finnGjeldendeAktivitetFor(UttakResultatPerioderEntitet gjeldeneperioder,
                                                                                 LocalDateInterval periodeInterval,
                                                                                 String arbeidsforholdId,
                                                                                 String arbeidsforholdOrgnr,
                                                                                 UttakArbeidType uttakArbeidType) {
        return finnGjeldendeAktivitetFor(finnGjeldendePeriodeFor(gjeldeneperioder, periodeInterval), arbeidsforholdId, arbeidsforholdOrgnr, uttakArbeidType);
    }
}
