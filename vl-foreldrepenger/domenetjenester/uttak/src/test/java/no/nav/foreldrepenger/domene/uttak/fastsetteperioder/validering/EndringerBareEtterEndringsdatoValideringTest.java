package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.exception.TekniskException;

public class EndringerBareEtterEndringsdatoValideringTest {

    @Test(expected = TekniskException.class)
    public void endring_av_periode_før_endringsdato_skal_føre_til_valideringsfeil() {
        ArrayList<UttakResultatPeriode> perioder = new ArrayList<>();
        perioder.add(periode(LocalDate.of(2017, 12, 1), LocalDate.of(2017, 12, 31)));
        EndringerBareEtterEndringsdatoValidering validering = new EndringerBareEtterEndringsdatoValidering();

        validering.utfør(new UttakResultatPerioder(perioder, Optional.of(LocalDate.of(2018, 1, 1))));

        //Validering skal feile
    }

    @Test
    public void endring_av_periode_med_start_på_endringsdato_skal_ikke_føre_til_valideringsfeil() {
        ArrayList<UttakResultatPeriode> perioder = new ArrayList<>();
        perioder.add(periode(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 31)));
        EndringerBareEtterEndringsdatoValidering validering = new EndringerBareEtterEndringsdatoValidering();

        validering.utfør(new UttakResultatPerioder(perioder, Optional.of(LocalDate.of(2018, 1, 1))));

        //Validering OK
    }

    @Test
    public void endring_av_periode_med_start_etter_endringsdato_skal_ikke_føre_til_valideringsfeil() {
        ArrayList<UttakResultatPeriode> perioder = new ArrayList<>();
        perioder.add(periode(LocalDate.of(2018, 1, 12), LocalDate.of(2018, 1, 31)));
        EndringerBareEtterEndringsdatoValidering validering = new EndringerBareEtterEndringsdatoValidering();

        validering.utfør(new UttakResultatPerioder(perioder, Optional.of(LocalDate.of(2018, 1, 1))));

        //Validering OK
    }

    @Test
    public void dersom_det_ikke_er_satt_endringsdato_så_skal_det_ikke_føre_til_valideringsfeil() {
        ArrayList<UttakResultatPeriode> perioder = new ArrayList<>();
        perioder.add(periode(LocalDate.of(2018, 1, 12), LocalDate.of(2018, 1, 31)));
        EndringerBareEtterEndringsdatoValidering validering = new EndringerBareEtterEndringsdatoValidering();

        validering.utfør(new UttakResultatPerioder(perioder, Optional.empty()));

        //Validering OK
    }

    private UttakResultatPeriode periode(LocalDate fom, LocalDate tom) {
        return new UttakResultatPeriode.Builder()
            .medTidsperiode(new LocalDateInterval(fom, tom))
            .medType(PeriodeResultatType.INNVILGET)
            .build();
    }


}
