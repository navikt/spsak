package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriodeAktivitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.exception.TekniskException;

public class IkkeNegativKontoValideringTest {

    @Test
    public void feilVedOverskridelseAvMaksDager() {
        int trekkdager1 = 10;
        int trekkdager2 = 30;
        int trekkdager3 = 20;
        UttakResultatPeriode gruppe1 = gruppeMedPerioder(aktivitet(trekkdager1, StønadskontoType.MØDREKVOTE));
        UttakResultatPeriode gruppe2 = gruppeMedPerioder(aktivitet(trekkdager2, StønadskontoType.MØDREKVOTE), aktivitet(trekkdager3, StønadskontoType.FELLESPERIODE));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(Arrays.asList(gruppe1, gruppe2));

        IkkeNegativKontoValidering validering = new IkkeNegativKontoValidering(Arrays.asList(
            konto(40, StønadskontoType.MØDREKVOTE),
            konto(15, StønadskontoType.FELLESPERIODE)
        ));
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void okHvisIkkeMaksDagerBlirOverskredet() {
        int trekkdager1 = 10;
        int trekkdager2 = 30;
        int trekkdager3 = 20;
        UttakResultatPeriode gruppe1 = gruppeMedPerioder(aktivitet(trekkdager1, StønadskontoType.MØDREKVOTE));
        UttakResultatPeriode gruppe2 = gruppeMedPerioder(aktivitet(trekkdager2, StønadskontoType.MØDREKVOTE), aktivitet(trekkdager3, StønadskontoType.FELLESPERIODE));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(Arrays.asList(gruppe1, gruppe2));

        IkkeNegativKontoValidering validering = new IkkeNegativKontoValidering(Arrays.asList(
            konto(50, StønadskontoType.MØDREKVOTE),
            konto(20, StønadskontoType.FELLESPERIODE),
            konto(0, StønadskontoType.FEDREKVOTE)
        ));
        assertThatCode(() -> validering.utfør(nyePerioder)).doesNotThrowAnyException();
    }

    @Test
    public void skal_telle_alle_resultattyper_det_er_trekkdager_som_teller() {

        UttakResultatPeriode gruppe1 = gruppeMedPerioder(PeriodeResultatType.INNVILGET, aktivitet(1, StønadskontoType.MØDREKVOTE));
        UttakResultatPeriode gruppe2 = gruppeMedPerioder(PeriodeResultatType.AVSLÅTT, aktivitet(1, StønadskontoType.MØDREKVOTE));
        UttakResultatPeriode gruppe3 = gruppeMedPerioder(PeriodeResultatType.IKKE_FASTSATT, aktivitet(1, StønadskontoType.MØDREKVOTE));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(Arrays.asList(gruppe1, gruppe2, gruppe3));

        IkkeNegativKontoValidering validering = new IkkeNegativKontoValidering(Collections.singletonList(
            konto(2, StønadskontoType.MØDREKVOTE)
        ));
        assertThatCode(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void okHvisIkkeMaksDagerBlirOverskredetSamtidigUttak() {
        int trekkdager1 = 20;
        int trekkdager2 = 30;
        int trekkdager3 = 30;
        UttakResultatPeriode gruppe1 = gruppeMedPerioder(true, true, aktivitet(trekkdager1, StønadskontoType.FELLESPERIODE));
        UttakResultatPeriode gruppe2 = gruppeMedPerioder(aktivitet(trekkdager2, StønadskontoType.FELLESPERIODE));
        UttakResultatPeriode gruppe3 = gruppeMedPerioder(aktivitet(trekkdager3, StønadskontoType.FEDREKVOTE));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(Arrays.asList(gruppe1, gruppe2, gruppe3));

        IkkeNegativKontoValidering validering = new IkkeNegativKontoValidering(Arrays.asList(
            konto(30, StønadskontoType.FLERBARNSDAGER),
            konto(100, StønadskontoType.FELLESPERIODE),
            konto(50, StønadskontoType.FEDREKVOTE)
        ));
        assertThatCode(() -> validering.utfør(nyePerioder)).doesNotThrowAnyException();
    }

    @Test
    public void feilVedOverskridelseAvMaksDagerSamtidigUttakMedFedrekvoten() {
        int trekkdager1 = 10;
        int trekkdager2 = 35;
        UttakResultatPeriode gruppe1 = gruppeMedPerioder(true, true, aktivitet(trekkdager1, StønadskontoType.FEDREKVOTE));
        UttakResultatPeriode gruppe2 = gruppeMedPerioder(aktivitet(trekkdager2, StønadskontoType.FEDREKVOTE));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(Arrays.asList(gruppe1, gruppe2));

        IkkeNegativKontoValidering validering = new IkkeNegativKontoValidering(Arrays.asList(
            konto(15, StønadskontoType.FLERBARNSDAGER),
            konto(40, StønadskontoType.FEDREKVOTE)
        ));
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }


    private Stønadskonto konto(int maxDager, StønadskontoType type) {
        return new Stønadskonto.Builder()
            .medMaxDager(maxDager)
            .medStønadskontoType(type)
            .build();
    }

    private UttakResultatPeriodeAktivitet aktivitet(int trekkdager, StønadskontoType stønadskontoType) {
        UttakResultatPeriodeAktivitet aktivitet = new UttakResultatPeriodeAktivitet.Builder()
            .medArbeidsprosent(BigDecimal.TEN)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .medTrekkdager(trekkdager)
            .medTrekkonto(stønadskontoType)
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .medArbeidsforholdOrgnr("orgnr")
            .build();
        return aktivitet;
    }

    private UttakResultatPeriode gruppeMedPerioder(UttakResultatPeriodeAktivitet... perioder) {
        return gruppeMedPerioder(PeriodeResultatType.INNVILGET, perioder);
    }

    private UttakResultatPeriode gruppeMedPerioder(PeriodeResultatType resultatType, UttakResultatPeriodeAktivitet... perioder) {
        return gruppeMedPerioder(false, false, resultatType, perioder);
    }

    private UttakResultatPeriode gruppeMedPerioder(boolean samtidigUttak, boolean flerbarnsdager, UttakResultatPeriodeAktivitet... perioder) {
        return gruppeMedPerioder(samtidigUttak, flerbarnsdager, PeriodeResultatType.INNVILGET, perioder);
    }

    private UttakResultatPeriode gruppeMedPerioder(boolean samtidigUttak, boolean flerbarnsdager, PeriodeResultatType resultatType, UttakResultatPeriodeAktivitet... aktiviteter) {

        UttakResultatPeriode.Builder periodeBuilder = new UttakResultatPeriode.Builder()
            .medTidsperiode(new LocalDateInterval(LocalDate.now(), LocalDate.now().plusWeeks(10)))
            .medAktiviteter(Arrays.asList(aktiviteter))
            .medType(resultatType)
            .medFlerbarnsdager(flerbarnsdager)
            .medSamtidigUttak(samtidigUttak);
        return periodeBuilder.build();
    }

}
