package no.nav.foreldrepenger.regler.uttak.felles.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

public class LukketPeriodeTest {

    @Test
    public void test_av_() throws Exception {
        LocalDate dag1 = LocalDate.of(2018, 1, 1);
        LocalDate dag2 = dag1.plusDays(1);
        LocalDate dag3 = dag1.plusDays(2);
        LocalDate dag4 = dag1.plusDays(3);

        assertThat(new LukketPeriode(dag2, dag2).erOmsluttetAv(new LukketPeriode(dag2, dag2))).isTrue();
        assertThat(new LukketPeriode(dag2, dag3).erOmsluttetAv(new LukketPeriode(dag2, dag3))).isTrue();
        assertThat(new LukketPeriode(dag2, dag3).erOmsluttetAv(new LukketPeriode(dag1, dag3))).isTrue();
        assertThat(new LukketPeriode(dag2, dag3).erOmsluttetAv(new LukketPeriode(dag2, dag4))).isTrue();
        assertThat(new LukketPeriode(dag2, dag3).erOmsluttetAv(new LukketPeriode(dag1, dag4))).isTrue();

        assertThat(new LukketPeriode(dag1, dag4).erOmsluttetAv(new LukketPeriode(dag1, dag3))).isFalse();
        assertThat(new LukketPeriode(dag1, dag4).erOmsluttetAv(new LukketPeriode(dag2, dag4))).isFalse();
        assertThat(new LukketPeriode(dag1, dag4).erOmsluttetAv(new LukketPeriode(dag2, dag2))).isFalse();
        assertThat(new LukketPeriode(dag1, dag4).erOmsluttetAv(new LukketPeriode(dag4, dag4))).isFalse();

    }
}
