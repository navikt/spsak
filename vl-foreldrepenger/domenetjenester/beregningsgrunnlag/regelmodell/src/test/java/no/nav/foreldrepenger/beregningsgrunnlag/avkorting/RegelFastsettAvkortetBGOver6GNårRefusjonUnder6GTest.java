package no.nav.foreldrepenger.beregningsgrunnlag.avkorting;


import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagAvkortetPrÅr;
import static no.nav.foreldrepenger.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.Grunnbeløp;
import no.nav.foreldrepenger.beregningsgrunnlag.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

/**
 * Testen kjøres når Regelflyt 8 blir implementert
 */

public class RegelFastsettAvkortetBGOver6GNårRefusjonUnder6GTest {

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    private static final BigDecimal GRUNNBELØP = BigDecimal.valueOf(90000);
    private static final long seksG = 90000 * 6;

    private static final String ORGNR1 = "123";
    private static final String ORGNR2 = "456";
    private static final String ORGNR3 = "789";
    private static final String ORGNR4 = "101112";


    @Test
    //Scenario 1: Arbeidsgiver har refusjonskrav er lik 6G
    public void skalBeregneNårRefusjonKravErLik6G() {
        //Arrange
        double bruttoBG = 672000d;
        double refusjonsKrav = seksG;
        double forventetRedusert = seksG;
        double forventetRedusertBrukersAndel = forventetRedusert - refusjonsKrav;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(1, Collections.singletonList(bruttoBG), Collections.singletonList(refusjonsKrav))
            .getBeregningsgrunnlagPerioder().get(0);

        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Collections.singletonList(forventetRedusertBrukersAndel));
        verifiserArbeidsgiversAndel(grunnlag, Collections.singletonList(refusjonsKrav), Collections.singletonList(refusjonsKrav));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 2: Arbeidsgiver har refusjonskrav mindre enn 6G
    public void skalBeregneNårRefusjonKravErMindreEnn6G() {
        //Arrange
        double bruttoBG = 672000d;
        double refusjonsKrav = 300000d;
        double forventetRedusert = seksG;
        double forventetRedusertBrukersAndel = forventetRedusert - refusjonsKrav;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(1, Collections.singletonList(bruttoBG), Collections.singletonList(refusjonsKrav))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Collections.singletonList(forventetRedusertBrukersAndel));
        verifiserArbeidsgiversAndel(grunnlag, Collections.singletonList(refusjonsKrav), Collections.singletonList(refusjonsKrav));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 3: Arbeidsgiver har ikke refusjonskrav
    public void skalBeregneNårRefusjonKravErLikNull() {
        //Arrange
        double bruttoBG = 672000d;
        double refusjonsKrav = 0d;
        double forventetRedusert = seksG;
        double forventetRedusertBrukersAndel = forventetRedusert - refusjonsKrav;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(1, Collections.singletonList(bruttoBG), Collections.singletonList(refusjonsKrav))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Collections.singletonList(forventetRedusertBrukersAndel));
        verifiserArbeidsgiversAndel(grunnlag, Collections.singletonList(refusjonsKrav), Collections.singletonList(refusjonsKrav));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 6: Brutto beregningsgrunnlag > 6G i ett av arbeidsforholdene, denne arbeidsgivers refusjonskrav er 6G, de andre arbeidsgiverne har ikke refusjonskrav
    public void skalBeregneNårRefusjonKravErNullForEnAvDeToArbeidsgiverne() {
        //Arrange
        double bruttoBG1 = 896000d;
        double bruttoBG2 = 448000d;
        double refusjonsKrav1 = seksG;
        double refusjonsKrav2 = 0d;

        double forventetRedusert = seksG;
        double forventetRedusert1 = seksG;
        double forventetRedusert2 = forventetRedusert - forventetRedusert1;
        double forventetRedusertBrukersAndel1 = forventetRedusert1 - refusjonsKrav1;
        double forventetRedusertBrukersAndel2 = forventetRedusert2 - refusjonsKrav2;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(2, Arrays.asList(bruttoBG1, bruttoBG2),
            Arrays.asList(refusjonsKrav1, refusjonsKrav2))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2), Arrays.asList(refusjonsKrav1, refusjonsKrav2));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 9: En arbeidsgiver har refusjonskrav < 6G, de andre arbeidsgiverne har ikke refusjonskrav
    public void skalBeregneNårRefusjonKravErNullForToAvDeTreArbeidsgiverne() {
        //Arrange
        double bruttoBG1 = 100000d;
        double bruttoBG2 = 400000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = 0d;
        double refusjonsKrav2 = 400000d;
        double refusjonsKrav3 = 0d;

        double forventetRedusert = seksG;
        double forventetRedusert2 = refusjonsKrav2;
        double forventetRedusert1 = (forventetRedusert - refusjonsKrav2) * bruttoBG1 / (bruttoBG1 + bruttoBG3);
        double forventetRedusert3 = (forventetRedusert - refusjonsKrav2) * bruttoBG3 / (bruttoBG1 + bruttoBG3);
        double forventetRedusertBrukersAndel1 = forventetRedusert1 - refusjonsKrav1;
        double forventetRedusertBrukersAndel2 = forventetRedusert2 - refusjonsKrav2;
        double forventetRedusertBrukersAndel3 = forventetRedusert3 - refusjonsKrav3;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
            Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2, forventetRedusertBrukersAndel3));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 10: Flere arbeidsgiver har refusjonskrav < brutto beregningsgrunnlag for arbeidsgiveren, totalt refusjonskrav < 6G
    public void skalBeregneNårRefusjonKravErNullForEnAvDeTreArbeidsgiverne() {
        //Arrange
        double bruttoBG1 = 400000d;
        double bruttoBG2 = 500000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = 300000d;
        double refusjonsKrav2 = 150000d;
        double refusjonsKrav3 = 0d;

        double forventetRedusert = seksG;
        double forventetRedusert1 = refusjonsKrav1;
        double forventetRedusert2 = (forventetRedusert - refusjonsKrav1) * bruttoBG2 / (bruttoBG2 + bruttoBG3);
        double forventetRedusert3 = (forventetRedusert - refusjonsKrav1) * bruttoBG3 / (bruttoBG2 + bruttoBG3);
        double forventetRedusertBrukersAndel1 = forventetRedusert1 - refusjonsKrav1;
        double forventetRedusertBrukersAndel2 = forventetRedusert2 - refusjonsKrav2;
        double forventetRedusertBrukersAndel3 = forventetRedusert3 - refusjonsKrav3;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
            Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2, forventetRedusertBrukersAndel3));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    //Scenario 10: Flere arbeidsgiver har refusjonskrav < brutto beregningsgrunnlag for arbeidsgiveren, totalt refusjonskrav < 6G
    public void skalBeregneNårAlleArbeidsgivereHarRefusjonKravMindreEnn6G() {
        //Arrange
        double bruttoBG1 = 400000d;
        double bruttoBG2 = 500000d;
        double bruttoBG3 = 250000d;
        double refusjonsKrav1 = 250000d;
        double refusjonsKrav2 = 50000d;
        double refusjonsKrav3 = 220000d;

        double forventetRedusert = seksG;
        double forventetRedusert1 = refusjonsKrav1;
        double forventetRedusert2 = (forventetRedusert - refusjonsKrav1 - refusjonsKrav3);
        double forventetRedusert3 = refusjonsKrav3;
        double forventetRedusertArbeidsgiver1 = refusjonsKrav1;
        double forventetRedusertBrukersAndel1 = forventetRedusert1 - forventetRedusertArbeidsgiver1;
        double forventetRedusertArbeidsgiver2 = refusjonsKrav2;
        double forventetRedusertBrukersAndel2 = forventetRedusert2 - forventetRedusertArbeidsgiver2;
        double forventetRedusertArbeidsgiver3 = refusjonsKrav3;
        double forventetRedusertBrukersAndel3 = forventetRedusert3 - forventetRedusertArbeidsgiver3;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlag(3, Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3),
            Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3))
            .getBeregningsgrunnlagPerioder().get(0);
        //Act
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBrukersAndel(grunnlag, Arrays.asList(forventetRedusertBrukersAndel1, forventetRedusertBrukersAndel2, forventetRedusertBrukersAndel3));
        verifiserArbeidsgiversAndel(grunnlag, Arrays.asList(refusjonsKrav1, refusjonsKrav2, refusjonsKrav3),
            Arrays.asList(forventetRedusertArbeidsgiver1, forventetRedusertArbeidsgiver2, forventetRedusertArbeidsgiver3));
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.ATFL, forventetRedusert);
    }

    @Test
    // Scenario 11: Flere arbeidsforhold og totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // arbeidsforhold arbeidstaker og dagpenger
    public void skalBeregneAvkortningAvUlikeBeregningsgrunnlagsandelerMedEnAndelUtenArbeidsforhold(){
        double bruttoBG1 = 300000d;
        double refusjonsKrav1 = 20000d;
        double bruttoBG2 = 263000d;

        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        BeregningsgrunnlagPrStatus bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1, bruttoBG1, 1, AktivitetStatus.ATFL, refusjonsKrav1);
        BeregningsgrunnlagPrStatus bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoBG2, 2, AktivitetStatus.DP, null);
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .build();
        Beregningsgrunnlag beregningsgrunnlag = opprettGrunnlag(periode);

        double forventetAvkortet = seksG - bruttoBG1;

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortet);
    }

    @Test
    // Scenario 12: Flere arbeidsforhold og totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // arbeidsforhold arbeidstaker
    // dagpenger
    // arbeidsavklaringspenger
    public void skalBeregneAvkortningAvUlikeBeregningsgrunnlagsandelerMedToAndelUtenArbeidsforhold(){
        double bruttoBG1 = 300000d;
        double bruttoBG2 = 130000d;
        double bruttoBG3 = 120000d;

        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        BeregningsgrunnlagPrStatus bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1,bruttoBG1, 1, AktivitetStatus.ATFL);
        BeregningsgrunnlagPrStatus bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2,bruttoBG2, 2, AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus bgpsAAP = lagBeregningsgrunnlagPrStatus(ORGNR3,bruttoBG3, 3, AktivitetStatus.AAP);
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .medBeregningsgrunnlagPrStatus(bgpsAAP)
            .build();
        Beregningsgrunnlag beregningsgrunnlag = opprettGrunnlag(periode);

        double forventetAvkortet = 130000d;
        double forventetAvkortet2 = seksG - bruttoBG1 - bruttoBG2;

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortet);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.AAP, forventetAvkortet2);
    }

    @Test
    // Scenario 13: Flere arbeidsforhold og totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // arbeidsforhold arbeidstaker
    // dagpenger
    // arbeidsavklaringspenger
    // selvstendig næringsdrivende
    public void skalBeregneAvkortningAvUlikeBeregningsgrunnlagsandelerMedFlereAndelUtenArbeidsforhold(){
        double bruttoBG1 = 300000d;
        double bruttoBG2 = 130000d;
        double bruttoBG3 = 110000d;
        double bruttoBG4 = 100000;

        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        BeregningsgrunnlagPrStatus bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1,bruttoBG1, 1, AktivitetStatus.ATFL);
        BeregningsgrunnlagPrStatus bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2,bruttoBG2, 2, AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus bgpsAAP = lagBeregningsgrunnlagPrStatus(ORGNR3,bruttoBG3, 3, AktivitetStatus.AAP);
        BeregningsgrunnlagPrStatus bgpsSN = lagBeregningsgrunnlagPrStatus(ORGNR4,bruttoBG4, 4, AktivitetStatus.SN);
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .medBeregningsgrunnlagPrStatus(bgpsAAP)
            .medBeregningsgrunnlagPrStatus(bgpsSN)
            .build();
        Beregningsgrunnlag beregningsgrunnlag = opprettGrunnlag(periode);

        double forventetAvkortet = 130000d;
        double forventetAvkortet2 = seksG - bruttoBG1 - bruttoBG2;
        double forventetAvkortet3 = 0d;

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortet);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.AAP, forventetAvkortet2);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.SN, forventetAvkortet3);
    }

    @Test
    // Scenario 14:En arbeidsforhold , frilanser og dagpenger.
    // totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // Brutto beregningsgrunnlag for andelen fra frilanser >= beregningsgrunnlag uten arbeidsforhold til fordeling
    public void skalBeregneAvkortningAvFrilanserMedBruttoBGForAndelenStørreEnnBGUtenArbeidsforhold(){
        double bruttoAT = 300000d;
        double refusjonsKrav = 20000d;
        double bruttoFL = 260000d;
        double bruttoDP = 60000;

        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        BeregningsgrunnlagPrStatus bgpsATFL = lagBGPrStatusATFL(ORGNR1, bruttoAT, bruttoFL, 1, refusjonsKrav*12);
        BeregningsgrunnlagPrStatus bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoDP, 2, AktivitetStatus.DP);
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .build();
        Beregningsgrunnlag beregningsgrunnlag = opprettGrunnlag(periode);

        double forventetAvkortetFrilanser = seksG - bruttoAT;
        double forventetAvkortetDP = 0d;

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser(grunnlag, null,  forventetAvkortetFrilanser);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortetDP);
    }

    @Test
    // Scenario 15: En arbeidsforhold , frilanser og dagpenger.
    // totalt brutto BG > 6G , total refusjonskrav < 6G,
    // totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold < 6G
    // Brutto beregningsgrunnlag for andelen fra frilanser <= beregningsgrunnlag uten arbeidsforhold til fordeling
    public void skalBeregneAvkortningAvFrilanserMedBruttoBGForAndelenMindreEnnBGUtenArbeidsforhold(){
        double bruttoAT = 300000d;
        double refusjonsKrav = 20000d;
        double bruttoFL = 200000d;
        double bruttoDP = 60000;

        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        BeregningsgrunnlagPrStatus bgpsATFL = lagBGPrStatusATFL(ORGNR1, bruttoAT, bruttoFL, 1, refusjonsKrav);
        BeregningsgrunnlagPrStatus bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoDP, 2, AktivitetStatus.DP);
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .build();
        Beregningsgrunnlag beregningsgrunnlag = opprettGrunnlag(periode);

        double forventetAvkortetFrilanser = bruttoFL;
        double forventetAvkortetDP = seksG - bruttoAT - bruttoFL;

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Evaluation evaluation = new RegelFullføreBeregningsgrunnlag(grunnlag).evaluer(grunnlag);
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);
        verifiserBeregningsgrunnlagAvkortetPrÅrFrilanser(grunnlag, null,  forventetAvkortetFrilanser);
        verifiserBeregningsgrunnlagAvkortetPrÅr(grunnlag, null, AktivitetStatus.DP, forventetAvkortetDP);
    }


    private BeregningsgrunnlagPrStatus lagBeregningsgrunnlagPrStatus(String orgNr, double brutto, int andelNr,
                                                                     AktivitetStatus aktivitetStatus) {
        return lagBeregningsgrunnlagPrStatus(orgNr, brutto, andelNr, aktivitetStatus, null);
    }


    private BeregningsgrunnlagPrStatus lagBeregningsgrunnlagPrStatus(String orgNr, double brutto, int andelNr,
                                                                     AktivitetStatus aktivitetStatus, Double refusjonskrav){
        BeregningsgrunnlagPrArbeidsforhold afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgNr))
            .medBeregnetPrÅr(BigDecimal.valueOf(brutto))
            .medRefusjonskravPrÅr(refusjonskrav == null ? null : BigDecimal.valueOf(refusjonskrav))
            .medAndelNr(andelNr)
            .build();
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medAndelNr(aktivitetStatus.equals(AktivitetStatus.ATFL) ? null : Integer.toUnsignedLong(andelNr))
            .medArbeidsforhold(afBuilder1)
            .build();
    }

    private BeregningsgrunnlagPrStatus lagBGPrStatusATFL(String orgNr, double bruttoAT, double bruttoFL, int andelNr, Double refusjonPrÅr){
        BeregningsgrunnlagPrArbeidsforhold afBuilderAT = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgNr))
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoAT))
            .medRefusjonskravPrÅr(refusjonPrÅr == null ? null : BigDecimal.valueOf(refusjonPrÅr))
            .medAndelNr(andelNr)
            .build();
        BeregningsgrunnlagPrArbeidsforhold afBuilderFL = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.builder().medAktivitet(Aktivitet.FRILANSINNTEKT).medOrgnr(orgNr).build())
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoFL))
            .medAndelNr(andelNr)
            .build();
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(afBuilderAT)
            .medArbeidsforhold(afBuilderFL)
            .build();
    }

    private Beregningsgrunnlag lagBeregningsgrunnlag(int antallArbeidsforhold, List<Double> bruttoBG, List<Double> refusjonsKrav) {
        assertThat(bruttoBG).hasSize(antallArbeidsforhold);
        assertThat(refusjonsKrav).hasSize(antallArbeidsforhold);
        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        long andelNr = 1;
        BeregningsgrunnlagPrArbeidsforhold afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR1))
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(0)))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(0)))
            .medAndelNr(andelNr++)
            .build();
        if (antallArbeidsforhold == 1) {
            BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .build();
            BeregningsgrunnlagPeriode periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
        }

        BeregningsgrunnlagPrArbeidsforhold afBuilder2 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR2))
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(1)))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(1)))
            .medAndelNr(andelNr++)
            .build();

        if (antallArbeidsforhold == 2) {
            BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .medArbeidsforhold(afBuilder2)
                .build();
            BeregningsgrunnlagPeriode periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
        }

        BeregningsgrunnlagPrArbeidsforhold afBuilder3 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR3))
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(2)))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(2)))
            .medAndelNr(andelNr++)
            .build();
        if (antallArbeidsforhold == 3) {
            BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
                .medAktivitetStatus(AktivitetStatus.ATFL)
                .medArbeidsforhold(afBuilder1)
                .medArbeidsforhold(afBuilder2)
                .medArbeidsforhold(afBuilder3)
                .build();
            BeregningsgrunnlagPeriode periode = bgBuilder
                .medBeregningsgrunnlagPrStatus(bgpsATFL)
                .build();
            return opprettGrunnlag(periode);
        }
        BeregningsgrunnlagPrArbeidsforhold afBuilder4 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR4))
            .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(3)))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(3)))
            .medAndelNr(andelNr++)
            .build();
        BeregningsgrunnlagPrStatus bgpsATFL = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL)
            .medArbeidsforhold(afBuilder1)
            .medArbeidsforhold(afBuilder2)
            .medArbeidsforhold(afBuilder3)
            .medArbeidsforhold(afBuilder4)
            .build();
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .build();
        return opprettGrunnlag(periode);
    }


    private Beregningsgrunnlag opprettGrunnlag(BeregningsgrunnlagPeriode periode) {
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        arbeidsforhold.forEach(af -> inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskilde(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(af.getArbeidsforhold())
            .medMåned(skjæringstidspunkt)
            .medInntekt(af.getBruttoPrÅr())
            .build()));
        return Beregningsgrunnlag.builder()
            .medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null)))
            .medBeregningsgrunnlagPeriode(periode)
            .medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100)
            .medInntektsgrunnlag(inntektsgrunnlag)
            .medSkjæringstidspunkt(skjæringstidspunkt)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), GRUNNBELØP.longValue(), GRUNNBELØP.longValue())))
            .build();
    }

    private void verifiserBrukersAndel(BeregningsgrunnlagPeriode grunnlag, List<Double> avkortet) {
        List<BigDecimal> brukersAvkortetAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream().
            map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetBrukersAndelPrÅr).collect(Collectors.toList());
        assertThat(brukersAvkortetAndel).hasSameSizeAs(avkortet);
        for (int ix = 0; ix < avkortet.size(); ix++) {
            assertThat(brukersAvkortetAndel.get(ix).doubleValue()).isCloseTo(avkortet.get(ix), within(0.01));
        }
    }

    private void verifiserArbeidsgiversAndel(BeregningsgrunnlagPeriode grunnlag, List<Double> maksimalRefusjon, List<Double> redusertRefusjon) {
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsgiversAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsgiversAndel).hasSameSizeAs(maksimalRefusjon);
        assertThat(arbeidsgiversAndel).hasSameSizeAs(redusertRefusjon);
        for (int ix = 0; ix < arbeidsgiversAndel.size(); ix++) {
            verifiserEnkeltAndel(arbeidsgiversAndel.get(ix), maksimalRefusjon.get(ix), redusertRefusjon.get(ix));
        }
    }

    private void verifiserEnkeltAndel(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, Double maksimalrefusjon, Double redusertrefusjon) {
        assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr().doubleValue()).isCloseTo(maksimalrefusjon, within(0.01));
        assertThat(arbeidsforhold.getRedusertRefusjonPrÅr().doubleValue()).isCloseTo(redusertrefusjon, within(0.01));
    }
}
