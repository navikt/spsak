package no.nav.foreldrepenger.domene.typer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

public class BeløpTest {

    @Test
    public void erNulltall_returnerer_false_for_null() {
        BigDecimal bd = null;
        Beløp beløp = new Beløp(bd);
        boolean actual = beløp.erNulltall();
        assertThat(actual).isFalse();
    }

    @Test
    public void erNulltall_returnerer_true_for_ZERO() {
        BigDecimal bd = BigDecimal.ZERO.setScale(10);
        Beløp beløp = new Beløp(bd);
        boolean actual = beløp.erNulltall();
        assertThat(actual).isTrue();
    }

    @Test
    public void erNulltall_returnerer_false_for_liten_desimal() {
        BigDecimal bd = BigDecimal.valueOf(0.00001);
        Beløp beløp = new Beløp(bd);
        boolean actual = beløp.erNulltall();
        assertThat(actual).isFalse();
    }

    @Test
    public void erNullEllerNulltall_detekterer_liten_desimal() {
        BigDecimal bd = new BigDecimal(0.00001);
        Beløp beløp = new Beløp(bd);
        boolean actual = beløp.erNullEllerNulltall();
        assertThat(actual).isFalse();
    }

    @Test
    public void erNullEllerNulltall_detekterer_null() {
        BigDecimal bd = null;
        Beløp beløp = new Beløp(bd);
        boolean actual = beløp.erNullEllerNulltall();
        assertThat(actual).isTrue();
    }

    @Test
    public void erNullEllerNulltall_detekterer_nulltall() {
        BigDecimal bd = BigDecimal.ZERO;
        Beløp beløp = new Beløp(bd);
        boolean actual = beløp.erNullEllerNulltall();
        assertThat(actual).isTrue();
    }
}
