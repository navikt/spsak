package no.nav.foreldrepenger.beregning.regler.feriepenger;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatAndel;
import no.nav.foreldrepenger.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Arbeidsforhold;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Dekningsgrad;
import no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.beregning.regelmodell.feriepenger.BeregningsresultatFeriepengerRegelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class RegelBeregnFeriepengerTest {

    private Arbeidsforhold arbeidsforhold1 = Arbeidsforhold.nyttArbeidsforhold("123456789");
    private Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforhold("234567890");


    //Eksempler tatt fra https://confluence.adeo.no/display/MODNAV/27c+Beregn+feriepenger+PK-51965+OMR-49

    //Eksempel 1 Mor
    @Test
    public void skalBeregneFeriepengerForMor() {
        BeregningsresultatPeriode periode1 = byggBRPeriode(LocalDate.of(2018, 1, 6), LocalDate.of(2018, 3, 9));
        BeregningsresultatPeriode periode2 = byggBRPeriode(LocalDate.of(2018, 3, 10), LocalDate.of(2018, 3, 16));
        byggAndelerForPeriode(periode1, 350, 600, arbeidsforhold1);
        byggAndelerForPeriode(periode1, 100, 500, arbeidsforhold2);
        byggAndelerForPeriode(periode2, 150, 400, arbeidsforhold1);

        BeregningsresultatPeriode periode1annenPart = byggBRPeriode(LocalDate.of(2018, 3, 10), LocalDate.of(2018, 3, 16));
        BeregningsresultatPeriode periode2annenPart  = byggBRPeriode(LocalDate.of(2018, 3, 17), LocalDate.of(2018, 3, 31));
        byggAndelerForPeriode(periode1annenPart, 200, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode2annenPart, 300, 0, arbeidsforhold1);

        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = Arrays.asList(periode1annenPart, periode2annenPart);
        BeregningsresultatFeriepengerRegelModell regelModell = BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(Arrays.asList(periode1, periode2))
            .medAnnenPartsBeregningsresultatPerioder(annenPartsBeregningsresultatPerioder)
            .medInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medAnnenPartsInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .medErForelder1(true)
            .build();

        RegelBeregnFeriepenger regel = new RegelBeregnFeriepenger();
        Evaluation evaluation = regel.evaluer(regelModell);
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(sporing).isNotNull();
        assertThat(regelModell.getFeriepengerPeriode().getFomDato()).isEqualTo(LocalDate.of(2018, 1, 6));
        assertThat(regelModell.getFeriepengerPeriode().getTomDato()).isEqualTo(LocalDate.of(2018, 3, 23));

        regelModell.getBeregningsresultatPerioder().stream().flatMap(p->p.getBeregningsresultatAndelList().stream())
            .forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1));
        BeregningsresultatAndel andelBruker1 = periode1.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver1 = periode1.getBeregningsresultatAndelList().get(1);
        BeregningsresultatAndel andelBruker2 = periode1.getBeregningsresultatAndelList().get(2);
        BeregningsresultatAndel andelArbeidsgiver2 = periode1.getBeregningsresultatAndelList().get(3);
        BeregningsresultatAndel andelBruker3 = periode2.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver3 = periode2.getBeregningsresultatAndelList().get(1);

        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(1606.5));
        assertThat(andelBruker2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(459));
        assertThat(andelArbeidsgiver1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(2754));
        assertThat(andelArbeidsgiver2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(2295));
        assertThat(andelBruker3.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(76.5));
        assertThat(andelArbeidsgiver3.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(204));
    }

    //Eksempel 1X Mor med avslag i første periode
    @Test
    public void skalBeregneFeriepengerForMorMedAvslagIFørstePeriode() {
        BeregningsresultatPeriode periode0 = byggBRPeriode(LocalDate.of(2018, 1, 5), LocalDate.of(2018, 1, 5));
        BeregningsresultatPeriode periode1 = byggBRPeriode(LocalDate.of(2018, 1, 6), LocalDate.of(2018, 3, 9));
        BeregningsresultatPeriode periode2 = byggBRPeriode(LocalDate.of(2018, 3, 10), LocalDate.of(2018, 3, 16));
        byggAndelerForPeriode(periode0, 0, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode1, 350, 600, arbeidsforhold1);
        byggAndelerForPeriode(periode1, 100, 500, arbeidsforhold2);
        byggAndelerForPeriode(periode2, 150, 400, arbeidsforhold1);

        BeregningsresultatPeriode periode1annenPart = byggBRPeriode(LocalDate.of(2018, 3, 10), LocalDate.of(2018, 3, 16));
        BeregningsresultatPeriode periode2annenPart  = byggBRPeriode(LocalDate.of(2018, 3, 17), LocalDate.of(2018, 3, 31));
        byggAndelerForPeriode(periode1annenPart, 200, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode2annenPart, 300, 0, arbeidsforhold1);

        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = Arrays.asList(periode1annenPart, periode2annenPart);
        BeregningsresultatFeriepengerRegelModell regelModell = BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(Arrays.asList(periode0, periode1, periode2))
            .medAnnenPartsBeregningsresultatPerioder(annenPartsBeregningsresultatPerioder)
            .medInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medAnnenPartsInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .medErForelder1(true)
            .build();

        RegelBeregnFeriepenger regel = new RegelBeregnFeriepenger();
        Evaluation evaluation = regel.evaluer(regelModell);
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(sporing).isNotNull();
        assertThat(regelModell.getFeriepengerPeriode().getFomDato()).isEqualTo(LocalDate.of(2018, 1, 6));
        assertThat(regelModell.getFeriepengerPeriode().getTomDato()).isEqualTo(LocalDate.of(2018, 3, 23));

        regelModell.getBeregningsresultatPerioder().stream().flatMap(p->p.getBeregningsresultatAndelList().stream())
            .forEach(andel -> {
                if (andel.getDagsats() > 0) {
                    assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1);
                } else {
                    assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).isEmpty();
                }
            });
        BeregningsresultatAndel andelBruker1 = periode1.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver1 = periode1.getBeregningsresultatAndelList().get(1);
        BeregningsresultatAndel andelBruker2 = periode1.getBeregningsresultatAndelList().get(2);
        BeregningsresultatAndel andelArbeidsgiver2 = periode1.getBeregningsresultatAndelList().get(3);
        BeregningsresultatAndel andelBruker3 = periode2.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver3 = periode2.getBeregningsresultatAndelList().get(1);

        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(1606.5));
        assertThat(andelBruker2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(459));
        assertThat(andelArbeidsgiver1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(2754));
        assertThat(andelArbeidsgiver2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(2295));
        assertThat(andelBruker3.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(76.5));
        assertThat(andelArbeidsgiver3.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(204));
    }

    //Eksempel 1 Far
    @Test
    public void skalBeregneFeriepengerForFar() {
        //Arrange
        BeregningsresultatPeriode periode1annenPart = byggBRPeriode(LocalDate.of(2018, 1, 6), LocalDate.of(2018, 3, 9));
        BeregningsresultatPeriode periode2annenPart  = byggBRPeriode(LocalDate.of(2018, 3, 10), LocalDate.of(2018, 3, 16));
        byggAndelerForPeriode(periode1annenPart, 0, 1, arbeidsforhold1);
        byggAndelerForPeriode(periode2annenPart, 0, 1, arbeidsforhold1);
        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = Arrays.asList(periode1annenPart, periode2annenPart);

        BeregningsresultatPeriode periode1 = byggBRPeriode(LocalDate.of(2018, 3, 10), LocalDate.of(2018, 3, 16));
        BeregningsresultatPeriode periode2 = byggBRPeriode(LocalDate.of(2018, 3, 17), LocalDate.of(2018, 3, 31));
        byggAndelerForPeriode(periode1, 150, 400, arbeidsforhold1);
        byggAndelerForPeriode(periode2, 460, 1000, arbeidsforhold1);

        BeregningsresultatFeriepengerRegelModell regelModell = BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(Arrays.asList(periode1, periode2))
            .medAnnenPartsBeregningsresultatPerioder(annenPartsBeregningsresultatPerioder)
            .medInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medAnnenPartsInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .medErForelder1(false).build();
        //Act
        RegelBeregnFeriepenger regel = new RegelBeregnFeriepenger();
        Evaluation evaluation = regel.evaluer(regelModell);
        String sporing = EvaluationSerializer.asJson(evaluation);

        //Assert
        assertThat(sporing).isNotNull();
        assertThat(regelModell.getFeriepengerPeriode().getFomDato()).isEqualTo(LocalDate.of(2018, 1, 6));
        assertThat(regelModell.getFeriepengerPeriode().getTomDato()).isEqualTo(LocalDate.of(2018, 3, 23));

        regelModell.getBeregningsresultatPerioder().stream().flatMap(p->p.getBeregningsresultatAndelList().stream())
            .forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1));
        BeregningsresultatAndel andelBruker1 = periode1.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver1 = periode1.getBeregningsresultatAndelList().get(1);
        BeregningsresultatAndel andelBruker2 = periode2.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver2 = periode2.getBeregningsresultatAndelList().get(1);

        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(76.5));
        assertThat(andelBruker2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(234.6));
        assertThat(andelArbeidsgiver1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(204));
        assertThat(andelArbeidsgiver2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(510));
    }

    //Eksempel 2 Mor
    @Test
    public void skalBeregneFeriepengerForMorEksempel2() {
        BeregningsresultatPeriode periode1 = byggBRPeriode(LocalDate.of(2018, 1, 17), LocalDate.of(2018, 3, 20));
        BeregningsresultatPeriode periode2 = byggBRPeriode(LocalDate.of(2018, 3, 21), LocalDate.of(2018, 3, 28));
        BeregningsresultatPeriode periode3 = byggBRPeriode(LocalDate.of(2018, 3, 29), LocalDate.of(2018, 4, 8));
        byggAndelerForPeriode(periode1, 350, 600, arbeidsforhold1);
        byggAndelerForPeriode(periode1, 100, 500, arbeidsforhold2);
        byggAndelerForPeriode(periode2, 0, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode3, 350, 600, arbeidsforhold1);

        BeregningsresultatPeriode periode1annenPart = byggBRPeriode(LocalDate.of(2018, 3, 21),  LocalDate.of(2018, 4, 15));
        byggAndelerForPeriode(periode1annenPart, 500, 0, arbeidsforhold1);
        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = Arrays.asList(periode1annenPart);

        BeregningsresultatFeriepengerRegelModell regelModell = BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(Arrays.asList(periode1, periode2, periode3))
            .medAnnenPartsBeregningsresultatPerioder(annenPartsBeregningsresultatPerioder)
            .medInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medAnnenPartsInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .medErForelder1(true)
            .build();

        RegelBeregnFeriepenger regel = new RegelBeregnFeriepenger();
        Evaluation evaluation = regel.evaluer(regelModell);
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(sporing).isNotNull();
        assertThat(regelModell.getFeriepengerPeriode().getFomDato()).isEqualTo(LocalDate.of(2018, 1, 17));
        assertThat(regelModell.getFeriepengerPeriode().getTomDato()).isEqualTo(LocalDate.of(2018, 4, 4));

        assertThat(regelModell.getBeregningsresultatPerioder().stream().flatMap(p->p.getBeregningsresultatAndelList().stream())
            .flatMap(a->a.getBeregningsresultatFeriepengerPrÅrListe().stream()).collect(Collectors.toList())).hasSize(6);
        periode1.getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1));
        periode2.getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).isEmpty());
        periode3.getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1));

        BeregningsresultatAndel andelBruker1 = periode1.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver1 = periode1.getBeregningsresultatAndelList().get(1);
        BeregningsresultatAndel andelBruker2 = periode1.getBeregningsresultatAndelList().get(2);
        BeregningsresultatAndel andelArbeidsgiver2 = periode1.getBeregningsresultatAndelList().get(3);
        BeregningsresultatAndel andelBruker4 = periode3.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver4 = periode3.getBeregningsresultatAndelList().get(1);

        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(1606.5));
        assertThat(andelBruker2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(459));
        assertThat(andelArbeidsgiver1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(2754));
        assertThat(andelArbeidsgiver2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(2295));
        assertThat(andelBruker4.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(178.5));
        assertThat(andelArbeidsgiver4.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(306));
    }

    //Eksempel 2 Far
    @Test
    public void skalBeregneFeriepengerForFarEksempel2() {
        BeregningsresultatPeriode periode1 = byggBRPeriode(LocalDate.of(2018, 3, 21), LocalDate.of(2018, 4, 15));
        byggAndelerForPeriode(periode1, 150, 400, arbeidsforhold1);

        BeregningsresultatPeriode periode1annenPart = byggBRPeriode(LocalDate.of(2018, 1, 17), LocalDate.of(2018, 3, 20));
        BeregningsresultatPeriode periode2annenPart  = byggBRPeriode(LocalDate.of(2018, 3, 21), LocalDate.of(2018, 3, 28));
        BeregningsresultatPeriode periode3annenPart  = byggBRPeriode(LocalDate.of(2018, 3, 29), LocalDate.of(2018, 4, 8));
        byggAndelerForPeriode(periode1annenPart, 500, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode2annenPart, 0, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode3annenPart, 500, 0, arbeidsforhold1);
        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = Arrays.asList(periode1annenPart, periode2annenPart, periode3annenPart);

        BeregningsresultatFeriepengerRegelModell regelModell = BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(Arrays.asList(periode1))
            .medAnnenPartsBeregningsresultatPerioder(annenPartsBeregningsresultatPerioder)
            .medInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medAnnenPartsInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .medErForelder1(false)
            .build();

        RegelBeregnFeriepenger regel = new RegelBeregnFeriepenger();
        Evaluation evaluation = regel.evaluer(regelModell);
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(sporing).isNotNull();
        assertThat(regelModell.getFeriepengerPeriode().getFomDato()).isEqualTo(LocalDate.of(2018, 1, 17));
        assertThat(regelModell.getFeriepengerPeriode().getTomDato()).isEqualTo(LocalDate.of(2018, 4, 3));

        assertThat(regelModell.getBeregningsresultatPerioder().stream().flatMap(p->p.getBeregningsresultatAndelList().stream())
            .flatMap(a->a.getBeregningsresultatFeriepengerPrÅrListe().stream()).collect(Collectors.toList())).hasSize(2);
        periode1.getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1));

        BeregningsresultatAndel andelBruker1 = periode1.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver1 = periode1.getBeregningsresultatAndelList().get(1);

        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(153));
        assertThat(andelArbeidsgiver1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(408));
    }

    @Test
    public void skalBeregneFeriepengerOverFlereÅr() {
        BeregningsresultatPeriode periode1 = byggBRPeriode(LocalDate.of(2018, 11, 1), LocalDate.of(2019, 1, 5)); //47 ukedager
        BeregningsresultatPeriode periode2 = byggBRPeriode(LocalDate.of(2019, 1, 6), LocalDate.of(2019, 2, 5)); // 22 ukedager
        BeregningsresultatPeriode periode3 = byggBRPeriode(LocalDate.of(2019, 2, 6), LocalDate.of(2019, 4, 16)); // 50 ukedager
        byggAndelerForPeriode(periode1, 1000, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode2, 0, 0, arbeidsforhold1);
        byggAndelerForPeriode(periode3, 500, 500, arbeidsforhold1);

        BeregningsresultatPeriode periode1annenPart = byggBRPeriode(LocalDate.of(2019, 1, 25), LocalDate.of(2019, 2, 15));
        byggAndelerForPeriode(periode1annenPart, 500, 0, arbeidsforhold1);
        List<BeregningsresultatPeriode> annenPartsBeregningsresultatPerioder = Arrays.asList(periode1annenPart);

        BeregningsresultatFeriepengerRegelModell regelModell = BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(Arrays.asList(periode1, periode2, periode3))
            .medAnnenPartsBeregningsresultatPerioder(annenPartsBeregningsresultatPerioder)
            .medInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medAnnenPartsInntektskategorier(Collections.singleton(Inntektskategori.SJØMANN))
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80)
            .medErForelder1(true)
            .build();

        RegelBeregnFeriepenger regel = new RegelBeregnFeriepenger();
        Evaluation evaluation = regel.evaluer(regelModell);
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(sporing).isNotNull();
        assertThat(regelModell.getFeriepengerPeriode().getFomDato()).isEqualTo(LocalDate.of(2018, 11, 1));
        assertThat(regelModell.getFeriepengerPeriode().getTomDato()).isEqualTo(LocalDate.of(2019, 2, 21));

        regelModell.getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(2));
        regelModell.getBeregningsresultatPerioder().get(1).getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).isEmpty());
        regelModell.getBeregningsresultatPerioder().get(2).getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1));
        BeregningsresultatAndel andelBruker1 = periode1.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelBruker2 = periode3.getBeregningsresultatAndelList().get(0);
        BeregningsresultatAndel andelArbeidsgiver = periode3.getBeregningsresultatAndelList().get(1);

        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(4386));
        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(1).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(408));

        assertThat(andelBruker2.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(612));
        assertThat(andelArbeidsgiver.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(612));
    }

    @Test
    public void skalBeregneFeriepengerUtenAnnenpart() {
        BeregningsresultatPeriode periode1 = byggBRPeriode(LocalDate.of(2018, 11, 1), LocalDate.of(2018, 12, 31)); //43 ukedager
        byggAndelerForPeriode(periode1, 1000, 0, arbeidsforhold1);

        BeregningsresultatFeriepengerRegelModell regelModell = BeregningsresultatFeriepengerRegelModell.builder()
            .medBeregningsresultatPerioder(Arrays.asList(periode1))
            .medAnnenPartsBeregningsresultatPerioder(Collections.emptyList())
            .medInntektskategorier(Collections.singleton(Inntektskategori.ARBEIDSTAKER))
            .medAnnenPartsInntektskategorier(Collections.emptySet())
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .medErForelder1(true)
            .build();

        RegelBeregnFeriepenger regel = new RegelBeregnFeriepenger();
        Evaluation evaluation = regel.evaluer(regelModell);
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(sporing).isNotNull();
        assertThat(regelModell.getFeriepengerPeriode().getFomDato()).isEqualTo(LocalDate.of(2018, 11, 1));
        assertThat(regelModell.getFeriepengerPeriode().getTomDato()).isEqualTo(LocalDate.of(2018, 12, 31));

        regelModell.getBeregningsresultatPerioder().get(0).getBeregningsresultatAndelList().forEach(andel -> assertThat(andel.getBeregningsresultatFeriepengerPrÅrListe()).hasSize(1));
        BeregningsresultatAndel andelBruker1 = periode1.getBeregningsresultatAndelList().get(0);

        assertThat(andelBruker1.getBeregningsresultatFeriepengerPrÅrListe().get(0).getÅrsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(4386));
    }

    private BeregningsresultatPeriode byggBRPeriode(LocalDate fom, LocalDate tom) {
        return BeregningsresultatPeriode.builder()
            .medPeriode(new LocalDateInterval(fom, tom))
            .build();
    }

    private void byggAndelerForPeriode(BeregningsresultatPeriode periode, int dagsats, int refusjon, Arbeidsforhold arbeidsforhold1) {
        BeregningsresultatAndel.builder()
            .medDagsats((long)dagsats)
            .medDagsatsFraBg((long)dagsats)
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBrukerErMottaker(true)
            .medArbeidsforhold(arbeidsforhold1)
            .build(periode);
        if (refusjon > 0) {
            BeregningsresultatAndel.builder()
                .medDagsats((long)refusjon)
                .medDagsatsFraBg((long)refusjon)
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBrukerErMottaker(false)
                .medArbeidsforhold(arbeidsforhold1)
                .build(periode);
        }
    }


}
