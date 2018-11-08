package no.nav.foreldrepenger.beregningsgrunnlag.fastsette;

import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilArbeidsforholdMedInntektsmelding;
import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.data.Offset;
import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.Grunnbeløp;
import no.nav.foreldrepenger.beregningsgrunnlag.RegelmodellOversetter;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class RegelFullføreBeregningsgrunnlagTest {

    private static Long generatedId = 1L;
    private final Offset<Double> offset = Offset.offset(0.01);

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

    private static final String ORGNR1 = "123";
    private static final String ORGNR2 = "456";
    private static final String ORGNR3 = "789";

    private static final String[] ORGNRS = {ORGNR1, ORGNR2, ORGNR3};

    @Test
    // Bruker er arbeidstaker
    // Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
    // Flere arbeidsgiver har refusjonskrav
    // Ingen naturalytelse bortfaller
    public void totaltBruttoBGUnder6GMedRefusjonUtenNaturalYtelseBortfallerForToArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 200000d;
        double refusjonskrav1 = 200000d;
        double bortfaltYtelse1 = 0d;

        double bruttoBG2 = 250000d;
        double refusjonskrav2 = 150000d;
        double bortfaltYtelse2 = 0d;

        double totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2;
        double forventetRedusertBrukersAndel1 = bruttoBG1 + bortfaltYtelse1 - refusjonskrav1;
        double forventetRedusertBrukersAndel2 = bruttoBG2 + bortfaltYtelse2 - refusjonskrav2;
        double forventetRedusertArbeidsgiver1 = refusjonskrav1;
        double forventetRedusertArbeidsgiver2 = refusjonskrav2;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, Arrays.asList(bruttoBG1, bruttoBG2), Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12),
            Arrays.asList(bortfaltYtelse1, bortfaltYtelse2))
            .getBeregningsgrunnlagPerioder().get(0);

        kjørRegel(grunnlag);
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

        //Assert
        assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
        assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
        assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
    }

    @Test
    // Bruker er arbeidstaker
    // Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
    // Flere arbeidsgiver har refusjonskrav
    // Naturalytelse for ein av arbeidsgiverane bortfaller
    public void totaltBruttoBGUnder6GMedRefusjonMedNaturalYtelseBortfallerForEnAvToArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 200000d;
        double refusjonskrav1 = 200000d;
        double bortfaltYtelse1 = 12000d;

        double bruttoBG2 = 250000d;
        double refusjonskrav2 = 150000d;
        double bortfaltYtelse2 = 0d;

        double totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2;
        double forventetRedusertBrukersAndel1 = bruttoBG1 + bortfaltYtelse1 - refusjonskrav1;
        double forventetRedusertBrukersAndel2 = bruttoBG2 + bortfaltYtelse2 - refusjonskrav2;
        double forventetRedusertArbeidsgiver1 = refusjonskrav1;
        double forventetRedusertArbeidsgiver2 = refusjonskrav2;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, Arrays.asList(bruttoBG1, bruttoBG2), Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12),
            Arrays.asList(bortfaltYtelse1, bortfaltYtelse2))
            .getBeregningsgrunnlagPerioder().get(0);

        kjørRegel(grunnlag);
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

        //Assert
        assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
        assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
        assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
    }

    @Test
    // Bruker er arbeidstaker
    // Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
    // Flere arbeidsgiver har refusjonskrav
    // Naturalytelse for begge arbeidsgiverane bortfaller
    public void totaltBruttoBGUnder6GMedRefusjonMedNaturalYtelseBortfallerForBeggeArbeidsgivere() {
        //Arrange
        double bruttoBG1 = 200000d;
        double refusjonskrav1 = 200000d;
        double bortfaltYtelse1 = 12000d;

        double bruttoBG2 = 250000d;
        double refusjonskrav2 = 150000d;
        double bortfaltYtelse2 = 25000d;

        double totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2;
        double forventetRedusertBrukersAndel1 = bruttoBG1 + bortfaltYtelse1 - refusjonskrav1;
        double forventetRedusertBrukersAndel2 = bruttoBG2 + bortfaltYtelse2 - refusjonskrav2;
        double forventetRedusertArbeidsgiver1 = refusjonskrav1;
        double forventetRedusertArbeidsgiver2 = refusjonskrav2;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, Arrays.asList(bruttoBG1, bruttoBG2), Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12),
            Arrays.asList(bortfaltYtelse1, bortfaltYtelse2))
            .getBeregningsgrunnlagPerioder().get(0);
        kjørRegel(grunnlag);
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

        //Assert
        assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
        assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
        assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
    }


    @Test
    // Bruker er arbeidstaker
    // Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse < 6G
    // Flere arbeidsgiver har refusjonskrav
    // Naturalytelse for begge arbeidsgiverane bortfaller medfører BG over 6G
    public void totaltBruttoBGUnder6GMedRefusjonMedNaturalYtelseBortfallerForBeggeArbeidsgivereOgMedførerBGOver6G() {
        //Arrange
        double bruttoBG1 = GRUNNBELØP_2017 * 3; //Totalt under 6G
        double refusjonskrav1 = GRUNNBELØP_2017 * 3;
        double bortfaltYtelse1 = GRUNNBELØP_2017 * 0.3;

        double bruttoBG2 = GRUNNBELØP_2017 * 2.5; //Totalt under 6G
        double refusjonskrav2 = GRUNNBELØP_2017 * 1.5;
        double bortfaltYtelse2 = GRUNNBELØP_2017 * 0.3;

        double totaltBeregningsgrunnlag = bruttoBG1 + bruttoBG2 + bortfaltYtelse1 + bortfaltYtelse2; // Overstiger 6G
        double fraksjonBrukersAndel1 = (bruttoBG1 + bortfaltYtelse1) / totaltBeregningsgrunnlag;
        double fraksjonBrukersAndel2 = (bruttoBG2 + bortfaltYtelse2) / totaltBeregningsgrunnlag;
        double fordelingArbeidsforhold1 = GRUNNBELØP_2017 * 6 * fraksjonBrukersAndel1;
        double fordelingArbeidsforhold2 = GRUNNBELØP_2017 * 6 * fraksjonBrukersAndel2;
        double forventetRedusertArbeidsgiver1 = refusjonskrav1;
        double forventetRedusertArbeidsgiver2 = refusjonskrav2;
        double forventetRedusertBrukersAndel1 = fordelingArbeidsforhold1 - forventetRedusertArbeidsgiver1;
        double forventetRedusertBrukersAndel2 = fordelingArbeidsforhold2 - forventetRedusertArbeidsgiver2;

        BeregningsgrunnlagPeriode grunnlag = lagBeregningsgrunnlagMedBortfaltNaturalytelse(2, Arrays.asList(bruttoBG1, bruttoBG2), Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12),
            Arrays.asList(bortfaltYtelse1, bortfaltYtelse2))
            .getBeregningsgrunnlagPerioder().get(0);

        kjørRegel(grunnlag);
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsForhold = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();

        //Assert
        assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlag, offset);
        assertThat(arbeidsForhold.get(0).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(forventetRedusertBrukersAndel2, offset);
        assertThat(arbeidsForhold.get(0).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver1, offset);
        assertThat(arbeidsForhold.get(1).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(forventetRedusertArbeidsgiver2, offset);
    }

    @Test
    // Bruker er arbeidstaker
    // Har flere arbeidsforhold og totalt brutto beregningsgrunnlag for disse > 6G
    // brutto beregningsgrunnlag i de forskjellige arbeidsforholdene er < 6G
    // Flere arbeidsgiver har refusjonskrav < brutto beregningsgrunnlag for arbeidsgiveren
    // Totalt refusjonskrav < 6G
    // Naturalytelse bortfaller i alle arbeidsforholdene:
    //          - Naturalytelse for arbeidsgiver1 i 2. periode
    //          - Naturalytelse for arbeidsgiver2 i 3. periode
    //          - Naturalytelse for arbeidsgiver3 i 4. periode
    public void totaltBruttoBGOver6GMedRefusjonMedNaturalYtelseBortfallerForTreArbeidsgivere() {
        //Arrange
        double bruttoBG1 = GRUNNBELØP_2017 * 4; //Totalt over 6G
        double refusjonskrav1 = GRUNNBELØP_2017 * 3;
        double bortfaltYtelse1 = GRUNNBELØP_2017 * 0.24;

        double bruttoBG2 = GRUNNBELØP_2017 * 5; //Totalt over 6G
        double refusjonskrav2 = GRUNNBELØP_2017 * 1.5;
        double bortfaltYtelse2 = GRUNNBELØP_2017 * 0.5;

        double bruttoBG3 = GRUNNBELØP_2017 * 2.5; //Totalt over 6G
        double refusjonskrav3 = 0L;
        double bortfaltYtelse3 = GRUNNBELØP_2017 * 0.24;

        List<Double> refusjonsKrav = Arrays.asList(refusjonskrav1, refusjonskrav2, refusjonskrav3);
        List<Double> bruttoBG = Arrays.asList(bruttoBG1, bruttoBG2, bruttoBG3);

        double totaltBeregningsgrunnlagPeriode1 = bruttoBG1 + bruttoBG2 + bruttoBG3;
        // Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        List<Double> foreventetBrukersAndelerPeriode1 = getForventetBrukersAndeler(bruttoBG,
            Arrays.asList(0d, 0d, 0d), refusjonsKrav, GRUNNBELØP_2017 * 6.0);

        double totaltBeregningsgrunnlagPeriode2 = bruttoBG1 + bortfaltYtelse1 + bruttoBG2 + bruttoBG3;
        // Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        List<Double> foreventetBrukersAndelerPeriode2 = getForventetBrukersAndeler(bruttoBG,
            Arrays.asList(bortfaltYtelse1, 0d, 0d), refusjonsKrav, GRUNNBELØP_2017 * 6.0);

        double totaltBeregningsgrunnlagPeriode3 = bruttoBG1 + bortfaltYtelse1 + bruttoBG2 + bortfaltYtelse2 + bruttoBG3;
        // Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        List<Double> foreventetBrukersAndelerPeriode3 = getForventetBrukersAndeler(bruttoBG,
            Arrays.asList(bortfaltYtelse1, bortfaltYtelse2, 0d), refusjonsKrav, GRUNNBELØP_2017 * 6.0);

        double totaltBeregningsgrunnlagPeriode4 = bruttoBG1 + bortfaltYtelse1 + bruttoBG2 + bortfaltYtelse2 + bruttoBG3 + bortfaltYtelse3;
        // Andel for arbeidsforhold1 er mindre enn refusjon. Settes til 0.
        List<Double> foreventetBrukersAndelerPeriode4 = getForventetBrukersAndeler(bruttoBG,
            Arrays.asList(bortfaltYtelse1, bortfaltYtelse2, bortfaltYtelse3), refusjonsKrav, GRUNNBELØP_2017 * 6.0);


        // 1. periode: Ingen bortfalt ytelse for nokon av arbeidsgiverane
        BeregningsgrunnlagPeriode grunnlag1 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
            Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), Arrays.asList(0.0, 0.0, 0.0))
            .getBeregningsgrunnlagPerioder().get(0);

        // 2. periode: Bortfalt ytelse for arbeidsgiver1
        BeregningsgrunnlagPeriode grunnlag2 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
            Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), Arrays.asList(bortfaltYtelse1, 0.0, 0.0))
            .getBeregningsgrunnlagPerioder().get(0);

        // 3. periode: Bortfalt ytelse for arbeidsgiver1 og arbeidsgiver2
        BeregningsgrunnlagPeriode grunnlag3 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
            Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), Arrays.asList(bortfaltYtelse1, bortfaltYtelse2, 0.0))
            .getBeregningsgrunnlagPerioder().get(0);

        // 4. periode: Bortfalt ytelse alle arbeidsgivere
        BeregningsgrunnlagPeriode grunnlag4 = lagBeregningsgrunnlagMedBortfaltNaturalytelse(3, bruttoBG,
            Arrays.asList(refusjonskrav1 / 12, refusjonskrav2 / 12, refusjonskrav3 / 12), Arrays.asList(bortfaltYtelse1, bortfaltYtelse2, bortfaltYtelse3))
            .getBeregningsgrunnlagPerioder().get(0);

        kjørRegel(grunnlag1);
        kjørRegel(grunnlag2);
        kjørRegel(grunnlag3);
        kjørRegel(grunnlag4);

        //Assert
        // Periode 1
        assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode1, foreventetBrukersAndelerPeriode1, grunnlag1);

        // Periode 2
        assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode2, foreventetBrukersAndelerPeriode2, grunnlag2);

        // Periode 3
        assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode3, foreventetBrukersAndelerPeriode3, grunnlag3);

        // Periode 4
        assertPeriode(refusjonsKrav, totaltBeregningsgrunnlagPeriode4, foreventetBrukersAndelerPeriode4, grunnlag4);

    }

    private void assertPeriode(List<Double> refusjonskrav, double totaltBeregningsgrunnlagPeriode, List<Double> foreventetBrukersAndelerPeriode, BeregningsgrunnlagPeriode grunnlag) {
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsForholdEtterPeriode = grunnlag.getBeregningsgrunnlagPrStatus().iterator().next().getArbeidsforhold();
        assertThat(grunnlag.getBruttoPrÅrInkludertNaturalytelser().doubleValue()).isEqualTo(totaltBeregningsgrunnlagPeriode, offset);
        Double belopTilBetalingPeriode = 0d;
        for (int i = 0; i < arbeidsForholdEtterPeriode.size(); i++) {
            assertThat(arbeidsForholdEtterPeriode.get(i).getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(foreventetBrukersAndelerPeriode.get(i), offset);
            assertThat(arbeidsForholdEtterPeriode.get(i).getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(refusjonskrav.get(i), offset);
            belopTilBetalingPeriode += foreventetBrukersAndelerPeriode.get(i) + refusjonskrav.get(i);
        }
        assertThat(belopTilBetalingPeriode).isEqualTo(GRUNNBELØP_2017 * 6, offset);
    }


    private List<Double> getForventetBrukersAndeler(List<Double> brutto, List<Double> bortfalteYtelser, List<Double> refusjonsBelop, Double belopTilFordeling) {
        double forventetBG = brutto.stream().reduce(0d, (a, b) -> a + b) + bortfalteYtelser.stream().reduce(0d, (v1, v2) -> v1 + v2);
        Iterator<Double> byIterator = bortfalteYtelser.iterator();
        List<Double> fraksjonBrukersAndeler = brutto.stream().map(v -> (v + byIterator.next()) / forventetBG).collect(Collectors.toList());
        assertThat(fraksjonBrukersAndeler.stream().reduce(0d, (v1, v2) -> v1 + v2)).isEqualTo(1, offset);
        List<Double> fordelingArbeidsforhold = fraksjonBrukersAndeler.stream().map(v -> belopTilFordeling * v).collect(Collectors.toList());
        Iterator<Double> refusjonIterator = refusjonsBelop.iterator();
        List<Double> forventetBrukersAndeler = fordelingArbeidsforhold.stream().map(v -> (v - refusjonIterator.next())).collect(Collectors.toList());
        if (forventetBrukersAndeler.get(0) < 0) {
            if (brutto.size() > 1) {
                List<Double> restListe = getForventetBrukersAndeler(brutto.subList(1, brutto.size()), bortfalteYtelser.subList(1, brutto.size()),
                    refusjonsBelop.subList(1, brutto.size()), belopTilFordeling - refusjonsBelop.get(0));
                restListe.add(0, 0d);
                return restListe;
            }
            return Arrays.asList(0d);
        }
        return forventetBrukersAndeler;
    }


    @Test
    public void skalOppretteRegelmerknadForAvslagNårBruttoInntektPrÅrMindreEnnHalvG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.49;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr, offset);
    }

    @Test
    public void skalOppretteRegelmerknadForAvslagForFlereArbeidsforholdNårBruttoInntektPrÅrMindreEnnHalvG() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.25;
        double beregnetPrÅr2 = GRUNNBELØP_2017 * 0.24; //Totalt under 0,5G
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        assertThat(resultat.getMerknader().stream().map(RegelMerknad::getMerknadKode)).containsOnly("1041");
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(beregnetPrÅr + beregnetPrÅr2, offset);
    }

    @Test
    public void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesØvreGrenseScenario() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 5.99;
        totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesScenario(beregnetPrÅr);
    }

    @Test
    public void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesNedreGrenseScenario() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.50;
        totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesScenario(beregnetPrÅr);
    }

    private void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesScenario(double beregnetPrÅr) {
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr); //beregnetPrår = brutto = avkortet = redusert

        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold, beregnetPrÅr);
    }

    @Test
    public void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesNedreGrenseMedFlereArbeidsforholdScenario() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 0.25;
        double beregnetPrÅr2 = GRUNNBELØP_2017 * 0.15;
        double beregnetPrÅr3 = GRUNNBELØP_2017 * 0.11;
        double beregnetSum = beregnetPrÅr + beregnetPrÅr2 + beregnetPrÅr3; //Totalt rett over 0,5G
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
        leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr3, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetSum, beregnetSum, beregnetSum); //beregnetPrår = brutto = avkortet = redusert

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(0), beregnetPrÅr);
        verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(1), beregnetPrÅr2);
        verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(2), beregnetPrÅr3);
    }

    @Test
    public void totaltBruttoBGUnder6GUtenRefusjonSkalIkkeAvkortesØvreGrenseMedFlereArbeidsforholdScenario() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 3.0;
        double beregnetPrÅr2 = GRUNNBELØP_2017 * 2.0;
        double beregnetPrÅr3 = GRUNNBELØP_2017 * 0.99;
        double beregnetSum = beregnetPrÅr + beregnetPrÅr2 + beregnetPrÅr3; //Totalt rett under 6G
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0);
        leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr2, 0);
        leggTilArbeidsforhold(beregningsgrunnlag, beregnetPrÅr3, 0);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetSum, beregnetSum, beregnetSum); //beregnetPrår = brutto = avkortet = redusert

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(0), beregnetPrÅr);
        verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(1), beregnetPrÅr2);
        verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(arbeidsforhold.get(2), beregnetPrÅr3);
    }

    @Test
    public void totaltBruttoBGUnder6GMedRefusjonØvreGrenseScenario() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 5.99;
        double refusjonskrav = GRUNNBELØP_2017 * 3.0;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, refusjonskrav);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr);

        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, refusjonskrav, beregnetPrÅr, beregnetPrÅr,
            beregnetPrÅr, refusjonskrav,
            refusjonskrav, beregnetPrÅr - refusjonskrav, beregnetPrÅr - refusjonskrav);

    }

    @Test
    public void totaltBruttoBGUnder6GMedRefusjonStørreEnnBruttoBGScenario() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 4.0;
        double refusjonskrav = GRUNNBELØP_2017 * 5.0;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, refusjonskrav);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr);

        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, beregnetPrÅr, beregnetPrÅr, beregnetPrÅr,
            beregnetPrÅr, beregnetPrÅr, beregnetPrÅr, 0.0, 0.0);
    }

    @Test
    public void totaltBruttoBGUnder6GUtenRefusjonMedReduksjonScenario() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 4.0;
        double redusertPrÅr = 0.80 * beregnetPrÅr;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0.0, Dekningsgrad.DEKNINGSGRAD_80);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        verifiserBeregningsgrunnlag(resultat, grunnlag, beregnetPrÅr, beregnetPrÅr, redusertPrÅr);

        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, 0.0, beregnetPrÅr, beregnetPrÅr,
            redusertPrÅr, 0.0, 0.0, beregnetPrÅr, redusertPrÅr);
    }

    @Test
    public void totalBruttoOver6GRefusjonKravUnder6GTotalBGForArbeidsforholdUnder6G() {
        double bruttoATFL = 300000d;
        double refusjonsKrav = 20000d;
        double bruttoDP = 130000d;
        double bruttoAAP = 110000d;
        double bruttoSN = 100000;

        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));
        BeregningsgrunnlagPrStatus bgpsATFL = lagBeregningsgrunnlagPrStatus(ORGNR1, bruttoATFL, 1, AktivitetStatus.ATFL, refusjonsKrav*12);
        BeregningsgrunnlagPrStatus bgpsDP = lagBeregningsgrunnlagPrStatus(ORGNR2, bruttoDP, 2, AktivitetStatus.DP, refusjonsKrav*12);
        BeregningsgrunnlagPrStatus bgpsAAP = lagBeregningsgrunnlagPrStatus(ORGNR3, bruttoAAP, 3, AktivitetStatus.AAP, refusjonsKrav*12);
        BeregningsgrunnlagPrStatus bgpsSN = lagBeregningsgrunnlagPrStatus("112", bruttoSN, 4, AktivitetStatus.SN, refusjonsKrav*12);
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL)
            .medBeregningsgrunnlagPrStatus(bgpsDP)
            .medBeregningsgrunnlagPrStatus(bgpsAAP)
            .medBeregningsgrunnlagPrStatus(bgpsSN)
            .build();
        Beregningsgrunnlag beregningsgrunnlag = opprettGrunnlag(periode);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        double forventetAvkortet = 130000d;
        double forventetAvkortet2 = 110000d;
        double forventetAvkortet3 = GRUNNBELØP_2017 * 6 - bruttoATFL - bruttoDP - bruttoAAP;

        kjørRegel(grunnlag);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP).getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP).getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet2);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getAvkortetPrÅr().doubleValue()).isEqualTo(forventetAvkortet3);
    }

    @Test
    public void maksimalRefusjonSkalIkkeOverskrives() {
        //Arrange
        double beregnetPrÅr = GRUNNBELØP_2017 * 4.0;
        double maksimalRefusjonPrÅr = 238000;
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, 0.0, Dekningsgrad.DEKNINGSGRAD_80);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold bgPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(bgPrArbeidsforhold).medMaksimalRefusjonPrÅr(BigDecimal.valueOf(maksimalRefusjonPrÅr));

        //Act
        @SuppressWarnings("unused")
        RegelResultat resultat = kjørRegel(grunnlag);

        //Assert
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr()).isEqualTo(BigDecimal.valueOf(maksimalRefusjonPrÅr));
    }

    private RegelResultat kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
        RegelFullføreBeregningsgrunnlag regel = new RegelFullføreBeregningsgrunnlag(grunnlag);
        Evaluation evaluation = regel.evaluer(grunnlag);
        return RegelmodellOversetter.getRegelResultat(evaluation);
    }

    private void verifiserBeregningsgrunnlag(RegelResultat resultat, BeregningsgrunnlagPeriode grunnlag, double bruttoPrÅr,
                                             double avkortetPrÅr, double redusertPrÅr) {
        assertThat(resultat.getMerknader()).isEmpty();
        assertThat(grunnlag.getBruttoPrÅr().doubleValue()).isEqualTo(bruttoPrÅr, offset);
        assertThat(grunnlag.getAvkortetPrÅr().doubleValue()).isEqualTo(avkortetPrÅr, offset);
        assertThat(grunnlag.getRedusertPrÅr().doubleValue()).isEqualTo(redusertPrÅr, offset);
    }

    private void verifiserBgPrAfMedbruttoBGUnder6GUtenRefusjonUtenRedusering(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, double beregnetPrÅr) {
        verifiserBeregningsgrunnlagPrArbeidsforhold(arbeidsforhold, 0.0, beregnetPrÅr,
            beregnetPrÅr, beregnetPrÅr, 0.0, 0.0, beregnetPrÅr, beregnetPrÅr);
    }

    private void verifiserBeregningsgrunnlagPrArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, double maxRefusjon, double bruttoPrÅr,
                                                             double avkortetPrÅr, double redusertPrÅr,
                                                             double avkortetRefusjonPrÅr, double redusertRefusjonPrÅr, double avkortetBrukersAndelPrÅr, double redusertBrukersAndelPrÅr) {
        assertThat(arbeidsforhold.getMaksimalRefusjonPrÅr().doubleValue()).isEqualTo(maxRefusjon, offset);
        assertThat(arbeidsforhold.getBruttoPrÅr().doubleValue()).isEqualTo(bruttoPrÅr, offset);
        assertThat(arbeidsforhold.getAvkortetPrÅr().doubleValue()).isEqualTo(avkortetPrÅr, offset);
        assertThat(arbeidsforhold.getRedusertPrÅr().doubleValue()).isEqualTo(redusertPrÅr, offset);
        assertThat(arbeidsforhold.getAvkortetRefusjonPrÅr().doubleValue()).isEqualTo(avkortetRefusjonPrÅr, offset);
        assertThat(arbeidsforhold.getRedusertRefusjonPrÅr().doubleValue()).isEqualTo(redusertRefusjonPrÅr, offset);
        assertThat(arbeidsforhold.getAvkortetBrukersAndelPrÅr().doubleValue()).isEqualTo(avkortetBrukersAndelPrÅr, offset);
        assertThat(arbeidsforhold.getRedusertBrukersAndelPrÅr().doubleValue()).isEqualTo(redusertBrukersAndelPrÅr, offset);
        long dagsatsBruker = Math.round(redusertBrukersAndelPrÅr / 260);
        long dagsatsArbeidsgiver = Math.round(redusertRefusjonPrÅr / 260);
        assertThat(arbeidsforhold.getDagsatsBruker()).isEqualTo(dagsatsBruker);
        assertThat(arbeidsforhold.getDagsatsArbeidsgiver()).isEqualTo(dagsatsArbeidsgiver);
        assertThat(arbeidsforhold.getDagsats()).isEqualTo(dagsatsBruker + dagsatsArbeidsgiver);
    }


    private Beregningsgrunnlag opprettBeregningsgrunnlag(LocalDate skjæringstidspunkt, double beregnetPrÅr, double refusjonskravPrÅr) {
        return opprettBeregningsgrunnlag(skjæringstidspunkt, beregnetPrÅr, refusjonskravPrÅr, Dekningsgrad.DEKNINGSGRAD_100);
    }

    private Beregningsgrunnlag opprettBeregningsgrunnlag(LocalDate skjæringstidspunkt, double beregnetPrÅr, double refusjonskravPrÅr, Dekningsgrad dekningsgrad) {
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.valueOf(beregnetPrÅr / 12), BigDecimal.valueOf(refusjonskravPrÅr / 12));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        Beregningsgrunnlag.builder(beregningsgrunnlag).medDekningsgrad(dekningsgrad);

        BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
            .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr)).build();
        return beregningsgrunnlag;
    }

    private void leggTilArbeidsforhold(Beregningsgrunnlag grunnlag, double beregnetPrÅr, double refusjonskrav) {
        BeregningsgrunnlagPeriode bgPeriode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        String nyttOrgnr = generateId().toString();
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(nyttOrgnr);
        leggTilArbeidsforholdMedInntektsmelding(bgPeriode, skjæringstidspunkt, BigDecimal.valueOf(beregnetPrÅr / 12), BigDecimal.valueOf(refusjonskrav / 12), arbeidsforhold, BigDecimal.ZERO, null);
        BeregningsgrunnlagPrStatus atfl = bgPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrArbeidsforhold bgpaf = atfl.getArbeidsforhold().stream()
            .filter(af -> af.getArbeidsforhold().getOrgnr().equals(nyttOrgnr)).findFirst().get();
        BeregningsgrunnlagPrArbeidsforhold.builder(bgpaf)
            .medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr))
            .build();
    }


    private Beregningsgrunnlag opprettGrunnlag(BeregningsgrunnlagPeriode periode) {
        Beregningsgrunnlag.Builder grunnlagsBuilder = Beregningsgrunnlag.builder();
        grunnlagsBuilder.medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL, null)));
        grunnlagsBuilder.medSkjæringstidspunkt(skjæringstidspunkt);
        grunnlagsBuilder.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017));
        grunnlagsBuilder.medRedusertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017));
        grunnlagsBuilder.medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_100);
        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        arbeidsforhold.forEach(af -> {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskilde(Inntektskilde.INNTEKTSMELDING)
                .medArbeidsgiver(af.getArbeidsforhold())
                .medMåned(skjæringstidspunkt)
                .medInntekt(af.getBruttoPrÅr())
                .build());
        });
        grunnlagsBuilder.medBeregningsgrunnlagPeriode(periode)
            .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099, Month.DECEMBER, 31), GRUNNBELØP_2017, GRUNNBELØP_2017)))
            .medInntektsgrunnlag(inntektsgrunnlag);
        return grunnlagsBuilder.build();
    }

    private Beregningsgrunnlag lagBeregningsgrunnlagMedBortfaltNaturalytelse(int antallArbeidsforhold, List<Double> bruttoBG, List<Double> refusjonsKrav, List<Double> bortfalteYtelserPerArbeidsforhold) {
        assertThat(bruttoBG).hasSize(antallArbeidsforhold);
        assertThat(refusjonsKrav).hasSize(antallArbeidsforhold);
        assertThat(bortfalteYtelserPerArbeidsforhold).hasSize(antallArbeidsforhold);
        BeregningsgrunnlagPeriode.Builder bgBuilder = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(skjæringstidspunkt, null));

        BeregningsgrunnlagPrStatus.Builder prStatusBuilder = BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(AktivitetStatus.ATFL);
        for (int i = 0; i < antallArbeidsforhold; i++) {
            BeregningsgrunnlagPrArbeidsforhold afBuilder = BeregningsgrunnlagPrArbeidsforhold.builder()
                .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNRS[i]))
                .medAndelNr(i + 1)
                .medBeregnetPrÅr(BigDecimal.valueOf(bruttoBG.get(i)))
                .medNaturalytelseBortfaltPrÅr(BigDecimal.valueOf(bortfalteYtelserPerArbeidsforhold.get(i)))
                .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav.get(i)*12))
                .build();
            prStatusBuilder.medArbeidsforhold(afBuilder);
        }

        BeregningsgrunnlagPrStatus bgpsATFL1 = prStatusBuilder.build();
        BeregningsgrunnlagPeriode periode = bgBuilder
            .medBeregningsgrunnlagPrStatus(bgpsATFL1)
            .build();

        return opprettGrunnlag(periode);
    }

    private BeregningsgrunnlagPrStatus lagBeregningsgrunnlagPrStatus(String orgNr, double brutto, int andelNr,
                                                                     AktivitetStatus aktivitetStatus, double refusjonsKrav) {
        BeregningsgrunnlagPrArbeidsforhold afBuilder1 = BeregningsgrunnlagPrArbeidsforhold.builder()
            .medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgNr))
            .medBeregnetPrÅr(BigDecimal.valueOf(brutto))
            .medRefusjonskravPrÅr(BigDecimal.valueOf(refusjonsKrav))
            .medAndelNr(andelNr)
            .build();
        return BeregningsgrunnlagPrStatus.builder()
            .medAktivitetStatus(aktivitetStatus)
            .medAndelNr(aktivitetStatus.equals(AktivitetStatus.ATFL) ? null : Integer.toUnsignedLong(andelNr))
            .medArbeidsforhold(afBuilder1)
            .build();
    }

    private static Long generateId() {
        return generatedId++;
    }
}
