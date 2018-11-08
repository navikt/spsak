package no.nav.foreldrepenger.beregningsgrunnlag.ytelse;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.Grunnbeløp;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.InntektPeriodeType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.TilstøtendeYtelseAndel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagAndelTilstøtendeYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;

public class RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelseTest {

    final private LocalDate skjæringstidspunkt = LocalDate.of(2017, 10, 1);
    final private long GRUNNBELØP = 90000;
    final private BigDecimal gVerdi = BigDecimal.valueOf(GRUNNBELØP);
    final private String ORGNR = "12345";

    @Test
    public void standardScenarioForeldrepengerTidligereArbeidstaker() {
        //Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse grunnlag = BeregningsgrunnlagFraTilstøtendeYtelse.builder()
            .medYtelse(TilstøtendeYtelse.builder()
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
                .medInntektskategorier(Arrays.asList(Inntektskategori.ARBEIDSTAKER))
                .medOpprinneligSkjæringstidspunkt(skjæringstidspunkt)
                .medRelatertYtelseType(RelatertYtelseType.FORELDREPENGER)
                .medKildeInfotrygd(true)
                .leggTilArbeidsforhold(TilstøtendeYtelseAndel.builder()
                    .medBeløp(BigDecimal.valueOf(2160))
                    .medHyppighet(InntektPeriodeType.DAGLIG)
                    .medArbeidsforholdFom(skjæringstidspunkt.minusYears(1))
                    .medArbeidsforholdTom(skjæringstidspunkt)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medOrgNr(ORGNR)
                    .build()).build())
            .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), GRUNNBELØP, GRUNNBELØP)))
            .build();

        //Act
        RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse regel = new RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse();
        regel.evaluer(grunnlag);

        //Assert
        verifiserBeregningsgrunnlagTY(grunnlag, 0.8, true, 561600, 1, 0, ORGNR);
    }

    @Test
    public void standardScenarioSykepengerTidligereArbeidstaker() {
        //Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse grunnlag = BeregningsgrunnlagFraTilstøtendeYtelse.builder()
            .medYtelse(TilstøtendeYtelse.builder()
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_65)
                .medInntektskategorier(Arrays.asList(Inntektskategori.ARBEIDSTAKER))
                .medOpprinneligSkjæringstidspunkt(skjæringstidspunkt)
                .medRelatertYtelseType(RelatertYtelseType.SVANGERSKAPSPENGER)
                .medKildeInfotrygd(true)
                .leggTilArbeidsforhold(TilstøtendeYtelseAndel.builder()
                    .medBeløp(BigDecimal.valueOf(10800))
                    .medHyppighet(InntektPeriodeType.UKENTLIG)
                    .medArbeidsforholdFom(skjæringstidspunkt.minusYears(1))
                    .medArbeidsforholdTom(skjæringstidspunkt)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medOrgNr(ORGNR)
                    .build()).build())
            .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), GRUNNBELØP, GRUNNBELØP)))
            .build();

        //Act
        RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse regel = new RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse();
        regel.evaluer(grunnlag);

        //Assert
        verifiserBeregningsgrunnlagTY(grunnlag, 0.8, false, 561600, 1, 0, ORGNR);
    }

    @Test
    public void standardScenarioSykepengerTidligereKombinertArbeidstakerOgJordbruker() {
        //Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse grunnlag = BeregningsgrunnlagFraTilstøtendeYtelse.builder()
            .medYtelse(TilstøtendeYtelse.builder()
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_65)
                .medInntektskategorier(Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.JORDBRUKER))
                .medOpprinneligSkjæringstidspunkt(skjæringstidspunkt)
                .medRelatertYtelseType(RelatertYtelseType.SVANGERSKAPSPENGER)
                .medKildeInfotrygd(true)
                .leggTilArbeidsforhold(TilstøtendeYtelseAndel.builder()
                    .medBeløp(BigDecimal.valueOf(10800))
                    .medHyppighet(InntektPeriodeType.UKENTLIG)
                    .medArbeidsforholdFom(skjæringstidspunkt.minusYears(1))
                    .medArbeidsforholdTom(skjæringstidspunkt)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medOrgNr(ORGNR)
                    .build())
                .leggTilArbeidsforhold(TilstøtendeYtelseAndel.builder()
                    .medBeløp(BigDecimal.valueOf(5400))
                    .medHyppighet(InntektPeriodeType.UKENTLIG)
                    .medArbeidsforholdFom(skjæringstidspunkt.minusYears(1))
                    .medArbeidsforholdTom(skjæringstidspunkt)
                    .medInntektskategori(Inntektskategori.JORDBRUKER)
                    .medAktivitetStatus(AktivitetStatus.SN)
                    .build())
                .build())
            .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), GRUNNBELØP, GRUNNBELØP)))
            .build();

        //Act
        RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse regel = new RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse();
        regel.evaluer(grunnlag);

        //Assert
        verifiserBeregningsgrunnlagTY(grunnlag, 0.8, false, 561600, 2, 0, ORGNR);
        verifiserBeregningsgrunnlagTY(grunnlag, 0.8, false, 280800, 2, 1, null);
    }

    @Test
    public void scenarioSykepengerInaktiv() {
        //Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse grunnlag = BeregningsgrunnlagFraTilstøtendeYtelse.builder()
            .medYtelse(TilstøtendeYtelse.builder()
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_65)
                .medInntektskategorier(Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER))
                .medOpprinneligSkjæringstidspunkt(skjæringstidspunkt)
                .medRelatertYtelseType(RelatertYtelseType.SYKEPENGER)
                .medKildeInfotrygd(true)
                .leggTilArbeidsforhold(TilstøtendeYtelseAndel.builder()
                    .medBeløp(BigDecimal.valueOf(24000))
                    .medHyppighet(InntektPeriodeType.MÅNEDLIG)
                    .medArbeidsforholdFom(skjæringstidspunkt.minusYears(1))
                    .medArbeidsforholdTom(skjæringstidspunkt)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medOrgNr(ORGNR)
                    .build()).build())
            .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), GRUNNBELØP, GRUNNBELØP)))
            .build();

        //Act
        RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse regel = new RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse();
        regel.evaluer(grunnlag);

        //Assert
        verifiserBeregningsgrunnlagTY(grunnlag, 0.65, true, 288000, 1, 0, ORGNR);
    }

    @Test
    public void scenarioForeldrepengerNyeArbeidsforhold() {
        //Arrange
        BeregningsgrunnlagFraTilstøtendeYtelse grunnlag = BeregningsgrunnlagFraTilstøtendeYtelse.builder()
            .medYtelse(TilstøtendeYtelse.builder()
                .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
                .medInntektskategorier(Arrays.asList(Inntektskategori.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER))
                .medOpprinneligSkjæringstidspunkt(skjæringstidspunkt)
                .medRelatertYtelseType(RelatertYtelseType.FORELDREPENGER)
                .medKildeInfotrygd(false)
                .leggTilArbeidsforhold(TilstøtendeYtelseAndel.builder()
                    .medBeløp(BigDecimal.valueOf(24000))
                    .medHyppighet(InntektPeriodeType.MÅNEDLIG)
                    .medArbeidsforholdFom(skjæringstidspunkt.minusYears(1))
                    .medArbeidsforholdTom(skjæringstidspunkt)
                    .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                    .medAktivitetStatus(AktivitetStatus.ATFL)
                    .medOrgNr(ORGNR)
                    .build()).build())
            .leggTilBeregningsgrunnlagAndel(BeregningsgrunnlagAndelTilstøtendeYtelse.builder()
                .medBeløp(BigDecimal.valueOf(350000))
                .medHyppighet(InntektPeriodeType.ÅRLIG)
                .medOrgnr("123")
                .medInntektskategori(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)
                .medAktivitetStatus(AktivitetStatus.SN)
                .medArbeidsperiodeTom(skjæringstidspunkt.plusMonths(1))
                .medArbeidsperiodeFom(skjæringstidspunkt.plusMonths(7))
                .medAktivitetStatus(AktivitetStatus.SN)
                .build())
            .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), GRUNNBELØP, GRUNNBELØP)))
            .build();

        //Act
        RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse regel = new RegelOpprettBeregningsgrunnlagsandelerForTilstøtendeYtelse();
        regel.evaluer(grunnlag);

        //Assert
        verifiserBeregningsgrunnlagTY(grunnlag, 1.0, true, null, 2, 0, "123");
        verifiserBeregningsgrunnlagTY(grunnlag, 1.0, true, null, 2, 1, ORGNR);
    }

    private void verifiserBeregningsgrunnlagTY(BeregningsgrunnlagFraTilstøtendeYtelse grunnlag, double dekningsgrad, boolean erGrunnbeløpRedusert, Integer beregnetPrÅr, int antallAndeler, int andelNr, String orgnr) {
        assertThat(grunnlag.getGrunnbeløp().compareTo(gVerdi)).isEqualTo(0);
        if (erGrunnbeløpRedusert) {
            assertThat(grunnlag.getRedusertGrunnbeløp().compareTo(gVerdi.multiply(BigDecimal.valueOf(dekningsgrad)))).isEqualTo(0);
        }
        assertThat(grunnlag.getBeregningsgrunnlagAndeler()).hasSize(antallAndeler);
        BeregningsgrunnlagAndelTilstøtendeYtelse andel = grunnlag.getBeregningsgrunnlagAndeler().get(andelNr);
        assertThat(andel.getArbeidsperiodeFom()).isNotNull();
        assertThat(andel.getArbeidsperiodeTom()).isNotNull();
        assertThat(andel.getOrgnr()).isEqualTo(orgnr);
        assertThat(andel.getAktivitetStatus()).isNotNull();
        assertThat(andel.getBeløp()).isNotNull();
        assertThat(andel.getHyppighet()).isNotNull();
        assertThat(andel.getInntektskategori()).isNotNull();
        if (beregnetPrÅr != null) {
            assertThat(andel.getBeregnetPrÅr()).isEqualTo(BigDecimal.valueOf(beregnetPrÅr));
        } else {
            assertThat(andel.getBeregnetPrÅr()).isNull();
        }
    }
}
