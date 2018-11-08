package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriode;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPeriodeAktivitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.exception.TekniskException;

public class BareSplittetPerioderValideringTest {

    @Test
    public void enPeriodeKanSplittesIToHvisSammeFomOgTom() {
        LocalDate opprinneligFom = LocalDate.now();
        LocalDate opprinneligTom = LocalDate.now().plusWeeks(3);
        LocalDate førsteTom = opprinneligFom.plusWeeks(2);
        LocalDate sisteFom = førsteTom.plusDays(1);
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(singletonList(gruppeMedDato(opprinneligFom, opprinneligTom)));
        List<UttakResultatPeriode> nyeGrupper = asList(gruppeMedDato(opprinneligFom, førsteTom), gruppeMedDato(sisteFom, opprinneligTom));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        BareSplittetPerioderValidering validering = new BareSplittetPerioderValidering(opprinnelig);
        assertThatCode(() -> validering.utfør(nyePerioder)).doesNotThrowAnyException();
    }

    @Test
    public void enPeriodeKanIkkeSplittesIToHvisFørsteFomFørOpprinneligFom() {
        LocalDate opprinneligFom = LocalDate.now();
        LocalDate opprinneligTom = LocalDate.now().plusWeeks(3);
        LocalDate førsteFom = opprinneligFom.minusDays(1);
        LocalDate førsteTom = opprinneligFom.plusWeeks(2);
        LocalDate sisteFom = førsteTom.plusDays(1);
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(singletonList(gruppeMedDato(opprinneligFom, opprinneligTom)));
        List<UttakResultatPeriode> nyeGrupper = asList(gruppeMedDato(førsteFom, førsteTom), gruppeMedDato(sisteFom, opprinneligTom));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        BareSplittetPerioderValidering validering = new BareSplittetPerioderValidering(opprinnelig);
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void enPeriodeKanIkkeSplittesIToHvisFørsteFomEtterOpprinneligFom() {
        LocalDate opprinneligFom = LocalDate.now();
        LocalDate opprinneligTom = LocalDate.now().plusWeeks(3);
        LocalDate førsteTom = opprinneligFom.plusWeeks(2);
        LocalDate førsteFom = opprinneligFom.plusDays(1);
        LocalDate sisteFom = førsteTom.plusDays(1);
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(singletonList(gruppeMedDato(opprinneligFom, opprinneligTom)));
        List<UttakResultatPeriode> nyeGrupper = asList(gruppeMedDato(førsteFom, førsteTom), gruppeMedDato(sisteFom, opprinneligTom));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        BareSplittetPerioderValidering validering = new BareSplittetPerioderValidering(opprinnelig);
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void enPeriodeKanIkkeSplittesIToHvisSisteTomFørOpprinneligTom() {
        LocalDate opprinneligFom = LocalDate.now();
        LocalDate opprinneligTom = LocalDate.now().plusWeeks(3);
        LocalDate førsteTom = opprinneligFom.plusWeeks(2);
        LocalDate sisteFom = førsteTom.plusDays(1);
        LocalDate sisteTom = opprinneligTom.minusDays(1);
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(singletonList(gruppeMedDato(opprinneligFom, opprinneligTom)));
        List<UttakResultatPeriode> nyeGrupper = asList(gruppeMedDato(opprinneligFom, førsteTom), gruppeMedDato(sisteFom, sisteTom));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        BareSplittetPerioderValidering validering = new BareSplittetPerioderValidering(opprinnelig);
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void enPeriodeKanSplittesITreHvisFomOgTomStemmerMedOpprinnelig() {
        LocalDate opprinneligFom = LocalDate.now();
        LocalDate opprinneligTom = LocalDate.now().plusWeeks(10);
        LocalDate førsteTom = opprinneligFom.plusWeeks(2);
        LocalDate andreFom = førsteTom.plusDays(1);
        LocalDate andreTom = andreFom.plusWeeks(2);
        LocalDate tredjeFom = andreTom.plusDays(1);
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(singletonList(gruppeMedDato(opprinneligFom, opprinneligTom)));
        List<UttakResultatPeriode> nyeGrupper = asList(gruppeMedDato(opprinneligFom, førsteTom),
            gruppeMedDato(andreFom, andreTom),
            gruppeMedDato(tredjeFom, opprinneligTom));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        BareSplittetPerioderValidering validering = new BareSplittetPerioderValidering(opprinnelig);
        assertThatCode(() -> validering.utfør(nyePerioder)).doesNotThrowAnyException();
    }

    @Test
    public void enPeriodeKanIkkeSplittesIToLikePerioder() {
        LocalDate opprinneligFom = LocalDate.now();
        LocalDate opprinneligTom = LocalDate.now().plusWeeks(3);
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(singletonList(gruppeMedDato(opprinneligFom, opprinneligTom)));
        List<UttakResultatPeriode> nyeGrupper = asList(gruppeMedDato(opprinneligFom, opprinneligTom), gruppeMedDato(opprinneligFom, opprinneligTom));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        BareSplittetPerioderValidering validering = new BareSplittetPerioderValidering(opprinnelig);
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    @Test
    public void feilVedOverlappendePerioder() {
        LocalDate førsteFom = LocalDate.now();
        LocalDate førsteTom = LocalDate.now().plusDays(5);
        LocalDate andreTom = førsteTom.plusDays(10);
        UttakResultatPerioder opprinnelig = new UttakResultatPerioder(singletonList(gruppeMedDato(førsteFom, andreTom)));
        List<UttakResultatPeriode> nyeGrupper = asList(gruppeMedDato(førsteFom, førsteFom), gruppeMedDato(førsteTom, andreTom));
        UttakResultatPerioder nyePerioder = new UttakResultatPerioder(nyeGrupper);

        BareSplittetPerioderValidering validering = new BareSplittetPerioderValidering(opprinnelig);
        assertThatThrownBy(() -> validering.utfør(nyePerioder)).isInstanceOf(TekniskException.class);
    }

    private UttakResultatPeriodeAktivitet aktivitet() {
        return new UttakResultatPeriodeAktivitet.Builder()
            .medArbeidsprosent(BigDecimal.TEN)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
    }

    private UttakResultatPeriode gruppeMedDato(LocalDate fom, LocalDate tom) {
        UttakResultatPeriodeAktivitet aktivitet = aktivitet();
        return new UttakResultatPeriode.Builder()
            .medTidsperiode(new LocalDateInterval(fom, tom))
            .medAktiviteter(Collections.singletonList(aktivitet))
            .medType(PeriodeResultatType.INNVILGET)
            .build();
    }
}
