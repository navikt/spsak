package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class PeriodeBeregnerTest {

    private List<UttakResultatPeriodeEntitet> uttakResultatPeriodeEntitetListe = new ArrayList<>();
    private List<UttakResultatPeriodeAktivitetEntitet> aktiviteter = new ArrayList<>();
    private UttakResultatPeriodeEntitet uttakResultatPeriode;
    private Optional<Stønadskonto> stønadsKontoForeldrepengerFørFødsel;

    @Before
    public void setup() {
        uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(LocalDate.now().withDayOfYear(1), LocalDate.now().withDayOfYear(30)).medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT).build();
        aktiviteter.add(new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode,
            new UttakAktivitetEntitet.Builder()
                .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr1").build(), ArbeidsforholdRef.ref("1")).build())
            .medTrekkdager(8).medUtbetalingsprosent(BigDecimal.ZERO)
            .medTrekkonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medArbeidsprosent(BigDecimal.valueOf(100)).build());
        aktiviteter.add(new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode,
            new UttakAktivitetEntitet.Builder()
                .medUttakArbeidType(UttakArbeidType.FRILANS)
                .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr2").build(), ArbeidsforholdRef.ref("2")).build())
            .medTrekkdager(5).medUtbetalingsprosent(BigDecimal.ZERO)
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medArbeidsprosent(BigDecimal.TEN).build());
        aktiviteter.stream().forEach(uttakResultatPeriode::leggTilAktivitet);
        uttakResultatPeriodeEntitetListe.add(uttakResultatPeriode);
        stønadsKontoForeldrepengerFørFødsel = Optional.of(new Stønadskonto.Builder().medMaxDager(15).build());
    }

    @Test
    public void skal_finne_at_alle_aktiviteter_har_null_utbetaling_i_periode() {
        assertThat(PeriodeBeregner.alleAktiviteterHarNullUtbetaling(aktiviteter)).isTrue();
    }

    @Test
    public void skal_returnere_false_hvis_aktivitet_har_utbetaling() {
        UttakResultatPeriodeAktivitetEntitet aktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode,
            new UttakAktivitetEntitet()).medTrekkdager(0).medUtbetalingsprosent(BigDecimal.valueOf(10l)).medTrekkonto(StønadskontoType.FELLESPERIODE).medArbeidsprosent(BigDecimal.valueOf(100)).build();
        aktiviteter.add(aktivitet);
        uttakResultatPeriode.leggTilAktivitet(aktivitet);
        assertThat(PeriodeBeregner.alleAktiviteterHarNullUtbetaling(aktiviteter)).isFalse();
    }

    @Test
    public void skal_finne_liste_med_fellesperiode() {
        assertThat(PeriodeBeregner.finnPerioderMedStønadskontoType(uttakResultatPeriodeEntitetListe, StønadskontoType.FELLESPERIODE)).hasSize(1);
    }

    @Test
    public void skal_finne_liste_med_fellesperiode_også_annen_periode() {
        UttakResultatPeriodeAktivitetEntitet aktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode,
            new UttakAktivitetEntitet()).medTrekkdager(0).medUtbetalingsprosent(BigDecimal.valueOf(10l)).medTrekkonto(StønadskontoType.MØDREKVOTE).medArbeidsprosent(BigDecimal.valueOf(100)).build();
        aktiviteter.add(aktivitet);
        uttakResultatPeriode.leggTilAktivitet(aktivitet);
        assertThat(PeriodeBeregner.finnPerioderMedStønadskontoType(uttakResultatPeriodeEntitetListe, StønadskontoType.FELLESPERIODE)).hasSize(1);
    }

    @Test
    public void skal_ikke_finne_feil_stønadskontotype() {
        assertThat(PeriodeBeregner.finnPerioderMedStønadskontoType(uttakResultatPeriodeEntitetListe, StønadskontoType.FORELDREPENGER)).isEmpty();
    }

    @Test
    public void skal_matche_uttaksaktiviteter_med_aktivitet_status() {
        assertThat(PeriodeBeregner.finnAktivitetMedStatusHvisFinnes(AktivitetStatus.ARBEIDSTAKER, aktiviteter, null, null)).isNotEmpty();
        assertThat(PeriodeBeregner.finnAktivitetMedStatusHvisFinnes(AktivitetStatus.FRILANSER, aktiviteter, null, null)).isNotEmpty();
        assertThat(PeriodeBeregner.finnAktivitetMedStatusHvisFinnes(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, aktiviteter, null, null)).isEmpty();

        aktiviteter.add(new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode,
            new UttakAktivitetEntitet.Builder().medUttakArbeidType(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr3").build(), ArbeidsforholdRef.ref("1")).build())
            .medTrekkdager(0).medUtbetalingsprosent(BigDecimal.ZERO)
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medArbeidsprosent(BigDecimal.TEN).build());
        assertThat(PeriodeBeregner.finnAktivitetMedStatusHvisFinnes(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, aktiviteter, null, null)).isNotEmpty();
    }

    @Test
    public void skal_finne_alle_tapte_dager_før_fødsel() {
        assertThat(PeriodeBeregner.beregnTapteDagerFørTermin(uttakResultatPeriodeEntitetListe, stønadsKontoForeldrepengerFørFødsel)).isEqualTo(stønadsKontoForeldrepengerFørFødsel.get().getMaxDager());
    }

    @Test
    public void skal_ikke_finne_noen_tapte_dager_før_fødsel() {
        aktiviteter.add(new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode,
            new UttakAktivitetEntitet.Builder().medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr3").build(), ArbeidsforholdRef.ref("1")).build())
            .medTrekkdager(15).medUtbetalingsprosent(BigDecimal.TEN)
            .medTrekkonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medArbeidsprosent(BigDecimal.valueOf(100)).build());
        assertThat(PeriodeBeregner.beregnTapteDagerFørTermin(uttakResultatPeriodeEntitetListe, stønadsKontoForeldrepengerFørFødsel)).isEqualTo(0);
    }

    @Test
    public void skal_finne_noen_tapte_dager_før_fødsel() {
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr3").build(), ArbeidsforholdRef.ref("1"))
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriode, uttakAktivitet)
            .medTrekkdager(8)
            .medUtbetalingsprosent(BigDecimal.TEN)
            .medTrekkonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medArbeidsprosent(BigDecimal.valueOf(100)).build();
        aktiviteter.add(periodeAktivitet);
        assertThat(PeriodeBeregner.beregnTapteDagerFørTermin(uttakResultatPeriodeEntitetListe, stønadsKontoForeldrepengerFørFødsel)).isEqualTo(7);
    }

    @Test
    public void skal_finne_noen_tapte_dager_før_fødsel_med_forskjellige_periodetyper() {
        UttakResultatPeriodeEntitet uttakResultatPeriodemedTapteDager = new UttakResultatPeriodeEntitet.Builder(LocalDate.now().withDayOfYear(1), LocalDate.now().withDayOfYear(30)).medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT).build();
        aktiviteter.add(new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriodemedTapteDager,
            new UttakAktivitetEntitet.Builder().medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
                .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr3").build(), ArbeidsforholdRef.ref("1")).build())
            .medTrekkdager(5).medUtbetalingsprosent(BigDecimal.TEN)
            .medTrekkonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)
            .medArbeidsprosent(BigDecimal.valueOf(100)).build());
        aktiviteter.add(new UttakResultatPeriodeAktivitetEntitet.Builder(uttakResultatPeriodemedTapteDager,
            new UttakAktivitetEntitet.Builder().medUttakArbeidType(UttakArbeidType.FRILANS)
                .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr("orgnr3").build(), ArbeidsforholdRef.ref("1")).build())
            .medTrekkdager(10).medUtbetalingsprosent(BigDecimal.TEN)
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medArbeidsprosent(BigDecimal.TEN).build());
        aktiviteter.stream().forEach(uttakResultatPeriodemedTapteDager::leggTilAktivitet);
        uttakResultatPeriodeEntitetListe.add(uttakResultatPeriodemedTapteDager);

        assertThat(PeriodeBeregner.beregnTapteDagerFørTermin(uttakResultatPeriodeEntitetListe, stønadsKontoForeldrepengerFørFødsel)).isEqualTo(10);

    }

}
