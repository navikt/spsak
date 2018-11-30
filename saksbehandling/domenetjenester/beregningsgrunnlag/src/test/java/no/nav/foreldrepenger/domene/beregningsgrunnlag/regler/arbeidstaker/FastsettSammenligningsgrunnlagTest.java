package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlagIPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker.FastsettSammenligningsgrunnlag;
import no.nav.vedtak.util.FPDateUtil;

public class FastsettSammenligningsgrunnlagTest {

    /*
    Scenarioer i testene er tatt fra https://confluence.adeo.no/display/MODNAV/3b+Fastsette+sammenligningsgrunnlagsperiode#FunksjonellogUX
     */

    private static final String FUNKSJONELT_TIDSOFFSET = FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE;
    private static final String FUNKSJONELT_TIDSOFFSET_AKTIVERT = FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_AKTIVERT;

    @Before
    public void setup() {
        System.setProperty(FUNKSJONELT_TIDSOFFSET_AKTIVERT, "true");
    }

    @AfterClass
    public static void after() {
        System.clearProperty(FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_AKTIVERT);
        FPDateUtil.init();
    }

    private void settSimulertNåtidTil(LocalDate dato) {
        Period periode = Period.between(LocalDate.now(), dato);
        System.setProperty(FUNKSJONELT_TIDSOFFSET, periode.toString());
        FPDateUtil.init();
    }

    //Eksempel 1
    @Test
    public void sammenligningsgrunnlagFørFristMedSisteInntektIkkeRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 4, 3));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 2, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 2, 28));
    }

    //Eksempel 2
    @Test
    public void sammenligningsgrunnlagFørFristMedSisteInntektRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 4, 3));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 4, 1), LocalDate.of(2019, 3, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 4, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 3, 31));
    }

    //Eksempel 3
    @Test
    public void sammenligningsgrunnlagFørIHelgFristMedSisteInntektIkkeRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 1, 7));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 2, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 11, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2018, 11, 30));
    }

    @Test
    public void sammenligningsgrunnlagEtterFristMedSisteInntektIkkeRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 10, 8));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 8, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 9, 30));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(275000));
    }

    @Test
    public void sammenligningsgrunnlagEtterFristMedSisteInntektRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 10, 8));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 9, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 9, 30));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
    }

    //Eksempel 4
    @Test
    public void sammenligningsgrunnlagBehandlingEtterStp() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 5, 11));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 12, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsGrunnlag();
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 5, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    public void skalIkkeLageNyttSammenligningsgrunnlagNårAlleredeEksisterer() {
        //Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018, 10, 10);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 12, 31));
        SammenligningsGrunnlag sg = SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(periode)
            .medRapportertPrÅr(BigDecimal.valueOf(55)).build();
        Beregningsgrunnlag.builder(beregningsgrunnlag).medSammenligningsgrunnlag(sg);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlag().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag hentetSg = grunnlag.getSammenligningsGrunnlag();
        assertThat(hentetSg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(55));
        assertThat(hentetSg.getSammenligningsperiode().getFom()).isEqualTo(periode.getFom());
        assertThat(hentetSg.getSammenligningsperiode().getTom()).isEqualTo(periode.getTom());
    }
}
