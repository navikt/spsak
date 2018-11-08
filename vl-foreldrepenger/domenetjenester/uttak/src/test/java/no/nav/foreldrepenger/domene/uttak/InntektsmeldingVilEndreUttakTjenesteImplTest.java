package no.nav.foreldrepenger.domene.uttak;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakUtsettelseType;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class InntektsmeldingVilEndreUttakTjenesteImplTest {

    private static final LocalDate NÅ = LocalDate.now();
    private static final LukketPeriode PERIODE_1 = new LukketPeriode(NÅ.minusWeeks(10), NÅ.minusWeeks(9));
    private static final LukketPeriode PERIODE_2 = new LukketPeriode(NÅ.minusWeeks(4), NÅ.minusWeeks(3));

    private static final String ORGNR_1 = "111222333";
    private static final String ORGNR_2 = "222222222";
    private static final String ARBEIDSFORHOLDID_1 = "1";

    @Test
    public void ingenEndringNårGraderingsperiodeStemmerOverensMedUttaksresultatperiodePåArbeidsprosent() {
        BigDecimal arbeidsprosent = BigDecimal.valueOf(40);

        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, true);
        UttakResultatPeriodeEntitet opprinneligPeriode2 = periode(PERIODE_2, false);
        leggTilAktivitet(opprinneligPeriode1, true, arbeidsprosent, ORGNR_1, ARBEIDSFORHOLDID_1);
        leggTilAktivitet(opprinneligPeriode2, false, BigDecimal.valueOf(100), ORGNR_1, ARBEIDSFORHOLDID_1);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Arrays.asList(opprinneligPeriode1, opprinneligPeriode2));

        List<Gradering> nyGradering = singletonList(lagGraderingPeriode(PERIODE_1, arbeidsprosent));
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_1, ARBEIDSFORHOLDID_1, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void ingenEndringNårGraderingsperiodeStemmerOverensMedUttaksresultatperiodePåArbeidsprosentFlereGraderinger() {
        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(40);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(80);

        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, true);
        UttakResultatPeriodeEntitet opprinneligPeriode2 = periode(PERIODE_2, true);
        leggTilAktivitet(opprinneligPeriode1, true, arbeidsprosent1, ORGNR_1, ARBEIDSFORHOLDID_1);
        leggTilAktivitet(opprinneligPeriode2, true, arbeidsprosent2, ORGNR_1, ARBEIDSFORHOLDID_1);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Arrays.asList(opprinneligPeriode1, opprinneligPeriode2));

        List<Gradering> nyGradering = Arrays.asList(lagGraderingPeriode(PERIODE_1, arbeidsprosent1), lagGraderingPeriode(PERIODE_2, arbeidsprosent2));
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_1, ARBEIDSFORHOLDID_1, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void endringNårGraderingsperiodeIkkeStemmerOverensMedUttaksresultatperiodePåArbeidsprosent() {
        BigDecimal opprinneligArbeidsprosent = BigDecimal.valueOf(40);
        BigDecimal nyArbeidsprosent = BigDecimal.valueOf(50);

        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, true);
        leggTilAktivitet(opprinneligPeriode1, true, opprinneligArbeidsprosent, ORGNR_1, ARBEIDSFORHOLDID_1);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode1));

        List<Gradering> nyGradering = singletonList(lagGraderingPeriode(PERIODE_1, nyArbeidsprosent));
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_1, ARBEIDSFORHOLDID_1, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårInntektsmeldingIkkeInneholderGraderingMenUttakInneholderGradering() {

        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, true);
        leggTilAktivitet(opprinneligPeriode1, true, BigDecimal.valueOf(80), ORGNR_1, ARBEIDSFORHOLDID_1);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode1));

        List<Gradering> nyGradering = Collections.emptyList();
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_1, ARBEIDSFORHOLDID_1, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårInntektsmeldingInneholderGraderingOgUttakInneholderGraderingFlereArbeidsforhold() {

        BigDecimal arbeidsprosent1 = BigDecimal.valueOf(80);
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(60);
        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, true);
        UttakResultatPeriodeEntitet opprinneligPeriode2 = periode(PERIODE_2, true);
        leggTilAktivitet(opprinneligPeriode1, true, arbeidsprosent1, ORGNR_1, ARBEIDSFORHOLDID_1);
        leggTilAktivitet(opprinneligPeriode2, true, arbeidsprosent2, ORGNR_2, null);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode1));

        List<Gradering> nyGradering = Arrays.asList(lagGraderingPeriode(PERIODE_1, arbeidsprosent1), lagGraderingPeriode(PERIODE_2, arbeidsprosent2));
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_1, ARBEIDSFORHOLDID_1, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårGraderingsperiodeIkkeStemmerOverensMedUttaksresultatperiodePåTidsperiode() {
        BigDecimal arbeidsprosent = BigDecimal.valueOf(40);

        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, true);
        leggTilAktivitet(opprinneligPeriode1, true, arbeidsprosent, ORGNR_1, ARBEIDSFORHOLDID_1);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode1));

        List<Gradering> nyGradering = singletonList(lagGraderingPeriode(PERIODE_2, arbeidsprosent));
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_1, ARBEIDSFORHOLDID_1, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårGraderingsperiodeStemmerOverensMedUttaksresultatperiodePåArbeidsprosentMenForskjelligVirksomhet() {
        BigDecimal arbeidsprosent = BigDecimal.valueOf(40);

        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, true);
        leggTilAktivitet(opprinneligPeriode1, true, arbeidsprosent, ORGNR_1, ARBEIDSFORHOLDID_1);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode1));

        List<Gradering> nyGradering = singletonList(lagGraderingPeriode(PERIODE_1, arbeidsprosent));
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_2, null, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårGraderingsperiodeMenUttakmanglerGradering() {

        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(PERIODE_1, false);
        leggTilAktivitet(opprinneligPeriode1, false, BigDecimal.valueOf(0), ORGNR_1, ARBEIDSFORHOLDID_1);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode1));

        List<Gradering> nyGradering = singletonList(lagGraderingPeriode(PERIODE_1, BigDecimal.valueOf(40)));
        Inntektsmelding inntektsmelding = inntektsmeldingMedGradering(ORGNR_1, ARBEIDSFORHOLDID_1, nyGradering);
        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void ingenEndringNårUtsettelseArbeidPeriodeStemmerOverensMedUttaksresultatperiode() {
        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.ARBEID);
        UttakResultatPeriodeEntitet urelatertPeriode = periode(PERIODE_2, false);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Arrays.asList(opprinneligPeriode, urelatertPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.ARBEID));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void endringNårUtsettelseArbeidPeriodeIkkeStemmerOverensMedUttaksresultatperiodePåTid() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.ARBEID);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom().plusDays(5), PERIODE_1.getTom(), UtsettelseÅrsak.ARBEID));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårUtsettelseArbeidPeriodeIkkeStemmerOverensMedUttaksresultatperiodePåType() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.ARBEID);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.FERIE));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void ikkeEndringNårUtsettelseArbeidPeriodeStemmerOverensMedUttaksresultatperiodePeriodeSplittet() {
        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(2)), UttakUtsettelseType.ARBEID);
        UttakResultatPeriodeEntitet opprinneligPeriode2 = periode(new LukketPeriode(opprinneligPeriode1.getTom().plusDays(1), opprinneligPeriode1.getTom().plusWeeks(1)), UttakUtsettelseType.ARBEID);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Arrays.asList(opprinneligPeriode1, opprinneligPeriode2));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(opprinneligPeriode1.getFom(), opprinneligPeriode2.getTom(), UtsettelseÅrsak.ARBEID));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void endringNårInntektsmeldingenHarUtsettelseArbeidMenUttakManglerUtsettelse() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, false);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.ARBEID));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårInntektsmeldingenIkkeHarUtsettelseArbeidMenUttakHarUtsettelse() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(2)), UttakUtsettelseType.ARBEID);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = Collections.emptyList();
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void ingenEndringNårUtsettelseArbeidPeriodeStemmerOverensMedUttaksresultatperiodeMenInntektsmeldingHarPeriodeMedFerie() {
        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.ARBEID);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = Arrays.asList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.ARBEID),
            UtsettelsePeriodeEntitet.utsettelse(PERIODE_2.getFom(), PERIODE_2.getTom(), UtsettelseÅrsak.FERIE));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void ingenEndringNårUtsettelseFeriePeriodeStemmerOverensMedUttaksresultatperiode() {
        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.FERIE);
        UttakResultatPeriodeEntitet urelatertPeriode = periode(PERIODE_2, false);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Arrays.asList(opprinneligPeriode, urelatertPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.FERIE));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void endringNårUtsettelseFeriePeriodeIkkeStemmerOverensMedUttaksresultatperiodePåTid() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.FERIE);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom().plusDays(5), PERIODE_1.getTom(), UtsettelseÅrsak.FERIE));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårUtsettelseFeriePeriodeIkkeStemmerOverensMedUttaksresultatperiodePåType() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.FERIE);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.ARBEID));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void ikkeEndringNårUtsettelseFeriePeriodeStemmerOverensMedUttaksresultatperiodePeriodeSplittet() {
        UttakResultatPeriodeEntitet opprinneligPeriode1 = periode(new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(2)), UttakUtsettelseType.FERIE);
        UttakResultatPeriodeEntitet opprinneligPeriode2 = periode(new LukketPeriode(opprinneligPeriode1.getTom().plusDays(1), opprinneligPeriode1.getTom().plusWeeks(1)), UttakUtsettelseType.FERIE);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Arrays.asList(opprinneligPeriode1, opprinneligPeriode2));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(opprinneligPeriode1.getFom(), opprinneligPeriode2.getTom(), UtsettelseÅrsak.FERIE));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void endringNårInntektsmeldingenHarUtsettelsFerieMenUttakManglerUtsettelse() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, false);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = singletonList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.FERIE));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void endringNårInntektsmeldingenIkkeHarUtsettelseFerieMenUttakHarUtsettelse() {

        UttakResultatPeriodeEntitet opprinneligPeriode = periode(new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(2)), UttakUtsettelseType.FERIE);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = Collections.emptyList();
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
    }

    @Test
    public void ingenEndringNårUtsettelseFeriePeriodeStemmerOverensMedUttaksresultatperiodeMenInntektsmeldingHarPeriodeMedArbeid() {
        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.FERIE);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        List<UtsettelsePeriode> inntektsmeldingUtsettelser = Arrays.asList(UtsettelsePeriodeEntitet.utsettelse(PERIODE_1.getFom(), PERIODE_1.getTom(), UtsettelseÅrsak.FERIE),
            UtsettelsePeriodeEntitet.utsettelse(PERIODE_2.getFom(), PERIODE_2.getTom(), UtsettelseÅrsak.ARBEID));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, inntektsmeldingUtsettelser);

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    @Test
    public void ingenGraderingEllerUtsettelseSkalIkkeGiEndring() {
        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, false);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        InntektsmeldingBuilder inntektsmelding = planInntektsmelding(ORGNR_1, null);

        leggTilAktivitet(opprinneligPeriode, false, BigDecimal.valueOf(0), ORGNR_1, null);

        assertThat(tjeneste(opprinneligUttak).graderingVilEndreUttak(mock(Behandling.class), inntektsmelding.build())).isFalse();
        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding.build())).isFalse();
        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding.build())).isFalse();
    }

    @Test
    public void ingenUtsettelseIInntektsmeldingUtsettelseFerieIUttakVilBareGiEndringPåUtsettelseFerie() {
        UttakResultatPeriodeEntitet opprinneligPeriode = periode(PERIODE_1, UttakUtsettelseType.FERIE);
        UttakResultatEntitet opprinneligUttak = opprinneligUttak(Collections.singletonList(opprinneligPeriode));
        Inntektsmelding inntektsmelding = inntektsmeldingMedUtsettelse(ORGNR_1, Collections.emptyList());

        assertThat(tjeneste(opprinneligUttak).utsettelseFerieVilEndreUttak(mock(Behandling.class), inntektsmelding)).isTrue();
        assertThat(tjeneste(opprinneligUttak).utsettelseArbeidVilEndreUttak(mock(Behandling.class), inntektsmelding)).isFalse();
    }

    private Inntektsmelding inntektsmeldingMedUtsettelse(String orgnr, List<UtsettelsePeriode> utsettelser) {
        InntektsmeldingBuilder builder = planInntektsmelding(orgnr, null);
        utsettelser.forEach(builder::leggTil);
        return builder.build();
    }

    private InntektsmeldingBuilder planInntektsmelding(String orgnr, String arbeidsforholdId) {
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .build();
        return InntektsmeldingBuilder.builder()
            .medArbeidsforholdId(arbeidsforholdId)
            .medInnsendingstidspunkt(LocalDateTime.now())
            .medVirksomhet(virksomhet);
    }

    private Inntektsmelding inntektsmeldingMedGradering(String orgnr, String arbeidsforholdId, List<Gradering> nyGradering) {
        InntektsmeldingBuilder builder = planInntektsmelding(orgnr, arbeidsforholdId);
        nyGradering.forEach(builder::leggTil);
        return builder.build();
    }

    private UttakResultatEntitet opprinneligUttak(List<UttakResultatPeriodeEntitet> opprinneligPerioder) {
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        for (UttakResultatPeriodeEntitet periode : opprinneligPerioder) {
            perioder.leggTilPeriode(periode);
        }
        return new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(perioder)
            .build();
    }

    private InntektsmeldingVilEndreUttakTjenesteImpl tjeneste(UttakResultatEntitet uttakResultat) {
        UttakRepository repository = mock(UttakRepository.class);
        when(repository.hentUttakResultat(any())).thenReturn(uttakResultat);
        return new InntektsmeldingVilEndreUttakTjenesteImpl(repository);
    }

    private Gradering lagGraderingPeriode(LukketPeriode periode, BigDecimal arbeidsprosent) {
        return new GraderingEntitet(periode.getFom(), periode.getTom(), arbeidsprosent);
    }

    private UttakResultatPeriodeAktivitetEntitet leggTilAktivitet(UttakResultatPeriodeEntitet uttakPeriode,
                                                                  boolean søktGraderingForAktivitet,
                                                                  BigDecimal arbeidsprosent,
                                                                  String orgNr,
                                                                  String arbeidsforholdId) {

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(new VirksomhetEntitet.Builder().medOrgnr(orgNr).build(), ArbeidsforholdRef.ref(arbeidsforholdId))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = UttakResultatPeriodeAktivitetEntitet.builder(uttakPeriode, uttakAktivitet)
            .medArbeidsprosent(arbeidsprosent)
            .medErSøktGradering(søktGraderingForAktivitet)
            .build();

        uttakPeriode.leggTilAktivitet(periodeAktivitet);
        return periodeAktivitet;
    }

    private UttakResultatPeriodeEntitet periode(LukketPeriode periode, boolean innvilgetGradering) {
        return new UttakResultatPeriodeEntitet.Builder(periode.getFom(), periode.getTom())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .medGraderingInnvilget(innvilgetGradering)
            .build();
    }

    private UttakResultatPeriodeEntitet periode(LukketPeriode periode, UttakUtsettelseType uttakUtsettelseType) {
        return new UttakResultatPeriodeEntitet.Builder(periode.getFom(), periode.getTom())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .medUtsettelseType(uttakUtsettelseType)
            .build();
    }
}

