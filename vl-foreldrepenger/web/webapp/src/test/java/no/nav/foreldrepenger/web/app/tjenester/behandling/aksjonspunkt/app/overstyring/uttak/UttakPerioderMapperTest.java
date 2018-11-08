package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.uttak;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.GraderingAvslagÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.OverstyringUttakDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeAktivitetLagreDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakResultatPeriodeLagreDto;

public class UttakPerioderMapperTest {

    private static final String ORGNR = "000000000";
    private static final String ARBEIDSFORHOLD_ID = "1234";

    @Test
    public void skalBrukeProsentArbeidFraTidligere() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusWeeks(1);
        UttakResultatPeriodeLagreDto periode = new UttakResultatPeriodeLagreDto.Builder()
            .medTidsperiode(fom, tom)
            .medAktiviteter(Collections.singletonList(minimal().build()))
            .medÅrsak(PeriodeResultatÅrsak.UKJENT)
            .build();
        OverstyringUttakDto dto = new OverstyringUttakDto(Collections.singletonList(periode));
        UttakResultatPeriodeEntitet opprinneligPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr(ORGNR).build(), ArbeidsforholdRef.ref(ARBEIDSFORHOLD_ID))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(opprinneligPeriode, uttakAktivitet)
            .medArbeidsprosent(BigDecimal.valueOf(77))
            .build();
        opprinneligPeriode.leggTilAktivitet(periodeAktivitet);
        UttakResultatPerioderEntitet uttakResultatPerioderEntitet = new UttakResultatPerioderEntitet();
        uttakResultatPerioderEntitet.leggTilPeriode(opprinneligPeriode);
        UttakResultatPerioder mapped = UttakPerioderMapper.map(dto.getPerioder(), uttakResultatPerioderEntitet);

        assertThat(mapped.getPerioder().get(0).getAktiviteter().get(0).getArbeidsprosent()).isEqualTo(periodeAktivitet.getArbeidsprosent());
    }

    @Test
    public void skalSkilleMellomFrilansOgAnnet() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusWeeks(1);
        UttakResultatPeriodeAktivitetLagreDto frilans = new UttakResultatPeriodeAktivitetLagreDto.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .build();
        UttakResultatPeriodeAktivitetLagreDto annet = new UttakResultatPeriodeAktivitetLagreDto.Builder()
            .medUttakArbeidType(UttakArbeidType.ANNET)
            .medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .build();
        UttakResultatPeriodeLagreDto periode = new UttakResultatPeriodeLagreDto.Builder()
            .medTidsperiode(fom, tom)
            .medÅrsak(PeriodeResultatÅrsak.UKJENT)
            .medAktiviteter(Arrays.asList(frilans, annet))
            .build();
        OverstyringUttakDto dto = new OverstyringUttakDto(Collections.singletonList(periode));
        UttakResultatPeriodeEntitet opprinneligPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakAktivitetEntitet opprinneligFrilans = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .build();
        UttakAktivitetEntitet opprinneligAnnet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.ANNET)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet1 = new UttakResultatPeriodeAktivitetEntitet.Builder(opprinneligPeriode, opprinneligFrilans)
            .medArbeidsprosent(BigDecimal.valueOf(77))
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet2 = new UttakResultatPeriodeAktivitetEntitet.Builder(opprinneligPeriode, opprinneligAnnet)
            .medArbeidsprosent(BigDecimal.valueOf(77))
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .build();
        opprinneligPeriode.leggTilAktivitet(periodeAktivitet1);
        opprinneligPeriode.leggTilAktivitet(periodeAktivitet2);
        UttakResultatPerioderEntitet uttakResultatPerioderEntitet = new UttakResultatPerioderEntitet();
        uttakResultatPerioderEntitet.leggTilPeriode(opprinneligPeriode);
        UttakResultatPerioder mapped = UttakPerioderMapper.map(dto.getPerioder(), uttakResultatPerioderEntitet);

        assertThat(mapped.getPerioder().get(0).getAktiviteter().get(0).getUttakArbeidType()).isIn(UttakArbeidType.FRILANS, UttakArbeidType.ANNET);
        assertThat(mapped.getPerioder().get(0).getAktiviteter().get(1).getUttakArbeidType()).isIn(UttakArbeidType.FRILANS, UttakArbeidType.ANNET);
    }

    @Test
    public void skalSetteGraderingAvslagÅrsakTilUkjentHvisGraderingInnvilget() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusWeeks(1);
        UttakResultatPeriodeAktivitetLagreDto aktivitetDto = new UttakResultatPeriodeAktivitetLagreDto.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .build();
        UttakResultatPeriodeLagreDto periode = new UttakResultatPeriodeLagreDto.Builder()
            .medTidsperiode(fom, tom)
            .medGraderingAvslåttÅrsak(GraderingAvslagÅrsak.GRADERING_FØR_UKE_7)
            .medGraderingInnvilget(true)
            .medAktiviteter(Collections.singletonList(aktivitetDto))
            .build();
        OverstyringUttakDto dto = new OverstyringUttakDto(Collections.singletonList(periode));
        UttakResultatPeriodeEntitet opprinneligPeriode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .medGraderingInnvilget(false)
            .medGraderingAvslagÅrsak(GraderingAvslagÅrsak.GRADERING_FØR_UKE_7)
            .build();
        UttakAktivitetEntitet opprinneligFrilans = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .build();
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(opprinneligPeriode, opprinneligFrilans)
            .medArbeidsprosent(BigDecimal.valueOf(77))
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medErSøktGradering(true)
            .build();
        opprinneligPeriode.leggTilAktivitet(periodeAktivitet);
        UttakResultatPerioderEntitet uttakResultatPerioderEntitet = new UttakResultatPerioderEntitet();
        uttakResultatPerioderEntitet.leggTilPeriode(opprinneligPeriode);
        UttakResultatPerioder mapped = UttakPerioderMapper.map(dto.getPerioder(), uttakResultatPerioderEntitet);

        assertThat(mapped.getPerioder().get(0).isGraderingInnvilget()).isTrue();
        assertThat(mapped.getPerioder().get(0).getGraderingAvslagÅrsak()).isEqualTo(GraderingAvslagÅrsak.UKJENT);
    }

    private UttakResultatPeriodeAktivitetLagreDto.Builder minimal() {
        return new UttakResultatPeriodeAktivitetLagreDto.Builder()
            .medArbeidsforholdOrgnr(ORGNR)
            .medArbeidsforholdId(ARBEIDSFORHOLD_ID)
            .medStønadskontoType(StønadskontoType.FELLESPERIODE)
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .medUtbetalingsgrad(BigDecimal.ZERO);
    }

}
