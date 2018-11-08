package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.exception.TekniskException;

public class PerioderHarFastsattResultatValideringTest {

    @Test
    public void feilVedPeriodeMedNullResultat() {
        List<UttakResultatPeriode> nyeGrupper = Collections.singletonList(gruppeMedResultat(null));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        PerioderHarFastsattResultatValidering validering = new PerioderHarFastsattResultatValidering();
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void feilVedPeriodeMedResultatIkkeFastsatt() {
        List<UttakResultatPeriode> nyeGrupper = Collections.singletonList(gruppeMedResultat(PeriodeResultatType.IKKE_FASTSATT));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        PerioderHarFastsattResultatValidering validering = new PerioderHarFastsattResultatValidering();
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void okHvisPerioderHarFastsattResultat() {
        List<UttakResultatPeriode> nyeGrupper = Collections.singletonList(gruppeMedResultat(PeriodeResultatType.INNVILGET));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        PerioderHarFastsattResultatValidering validering = new PerioderHarFastsattResultatValidering();
        assertThatCode(() -> validering.utfør(nyePerioder)).doesNotThrowAnyException();
    }

    private UttakResultatPeriode gruppeMedResultat(PeriodeResultatType resultatType) {
        return new UttakResultatPeriode.Builder()
            .medTidsperiode(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusDays(1)))
            .medType(resultatType)
            .build();
    }
}
