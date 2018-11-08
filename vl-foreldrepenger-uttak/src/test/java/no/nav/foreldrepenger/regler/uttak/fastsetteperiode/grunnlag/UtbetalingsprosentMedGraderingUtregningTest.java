package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class UtbetalingsprosentMedGraderingUtregningTest {

    @Test
    public void toArbeidsforholdMedEnGradertGirRedusertUtbetalingsprosent() {
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        BigDecimal arbeidsprosent1 = BigDecimal.ZERO;
        BigDecimal arbeidsprosent2 = BigDecimal.valueOf(20);
        BigDecimal stillingsprosent1 = BigDecimal.valueOf(50);
        BigDecimal stillingsprosent2 = BigDecimal.valueOf(100);
        LukketPeriode periode = new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10));
        ArbeidTidslinje tidslinje1 = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent1, stillingsprosent1, null))
                .build();
        ArbeidTidslinje tidslinje2 = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forGradertOrdinærtArbeid(arbeidsprosent2, stillingsprosent2))
                .build();
        AktivitetIdentifikator aktivitetIdentifikator1 = AktivitetIdentifikator.forArbeid("orgnr1", "id");
        AktivitetIdentifikator aktivitetIdentifikator2 = AktivitetIdentifikator.forArbeid("orgnr2", null);
        arbeidsprosenter.leggTil(aktivitetIdentifikator1, tidslinje1);
        arbeidsprosenter.leggTil(aktivitetIdentifikator2, tidslinje2);
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator1, periode).resultat()).isEqualTo(new BigDecimal("100.00"));
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator2, periode).resultat()).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    public void permisjonsprosentSkalIkkeSettesTilUtbetalingsprosent() {
        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        BigDecimal arbeidsprosent = BigDecimal.valueOf(20);
        BigDecimal stillingsprosent = BigDecimal.valueOf(100);
        BigDecimal permisjonsprosent = BigDecimal.TEN;
        LukketPeriode periode = new LukketPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10));
        ArbeidTidslinje tidslinje1 = new ArbeidTidslinje.Builder()
                .medArbeid(periode, Arbeid.forOrdinærtArbeid(arbeidsprosent, stillingsprosent, permisjonsprosent))
                .build();
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forArbeid("orgnr1", "id");
        arbeidsprosenter.leggTil(aktivitetIdentifikator, tidslinje1);
        assertThat(utregning(arbeidsprosenter, aktivitetIdentifikator, periode).resultat()).isEqualTo(new BigDecimal("80.00"));
    }


    private UtbetalingsprosentMedGraderingUtregning utregning(Arbeidsprosenter arbeidsprosenter,
                                                               AktivitetIdentifikator aktivitetIdentifikator,
                                                               LukketPeriode periode) {
        return new UtbetalingsprosentMedGraderingUtregning(arbeidsprosenter, aktivitetIdentifikator, periode);
    }
}