package no.nav.vedtak.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import no.nav.vedtak.konfig.LocalCdiRunner;

@RunWith(LocalCdiRunner.class)
public class FPDateUtilTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        setOffsetAktivert(true);
    }

    @After
    public void after() {
        clearOffsetProps();
        FPDateUtil.init();
    }

    @Test
    public void test_skal_ikke_bruke_offset_når_offset_aktivert_er_false() {
        setOffsetAktivert(false);
        Period period = Period.ofDays(5);
        setOffset(period);

        LocalDateTime kontroll = LocalDateTime.now();
        LocalDateTime medOffset = LocalDateTime.now(FPDateUtil.getOffset());

        assertThat(medOffset).isEqualToIgnoringSeconds(kontroll);
    }

    @Test
    public void test_dato_skal_være_to_dager_frem_i_tid() {
        Period period = Period.ofDays(2);
        setOffset(period);

        LocalDateTime kontroll = LocalDateTime.now().plus(period);
        LocalDateTime medOffset = LocalDateTime.now(FPDateUtil.getOffset());

        assertThat(medOffset).isEqualToIgnoringHours(kontroll); // ignorerer timer, minutter, sekunder
    }

    @Test
    public void test_dato_skal_være_to_dager_bak_i_tid() {
        Period period = Period.ofDays(-2);
        setOffset(period);

        LocalDateTime kontroll = LocalDateTime.now().plus(period);
        LocalDateTime medOffset = LocalDateTime.now(FPDateUtil.getOffset());

        assertThat(medOffset).isEqualToIgnoringHours(kontroll); // ignorerer timer, minutter, sekunder
    }

    @Test
    public void test_dato_skal_være_4_måneder_frem_i_tid() {
        Period period = Period.ofMonths(6);
        setOffset(period);

        LocalDate kontroll = LocalDate.now().plus(period);
        LocalDate medOffset = LocalDate.now(FPDateUtil.getOffset());

        assertThat(medOffset).isEqualTo(kontroll);
    }

    @Test
    public void test_dato_skal_være_6_måneder_bak_i_tid() {
        Period period = Period.ofMonths(-6);
        setOffset(period);

        LocalDate kontroll = LocalDate.now().plus(period);
        LocalDate medOffset = LocalDate.now(FPDateUtil.getOffset());

        assertThat(medOffset).isEqualTo(kontroll);
    }

    @Test
    public void test_dato_skal_være_1_år_1_mnd_og_1_dag_bak_i_tid() {
        Period period = Period.of(-1, -1, -1);
        setOffset(period);

        LocalDate kontroll = LocalDate.now().plus(period);
        LocalDate medOffset = LocalDate.now(FPDateUtil.getOffset());

        assertThat(medOffset).isEqualTo(kontroll);
    }

    @Test
    public void test_dato_skal_være_1_år_frem_og_1_dag_tilbake() {
        Period period = Period.ofYears(1).minusDays(1);
        setOffset(period);

        LocalDate kontroll = LocalDate.now().plus(period);
        LocalDate medOffset = LocalDate.now(FPDateUtil.getOffset());

        assertThat(medOffset).isEqualTo(kontroll);
    }

    private void setOffsetAktivert(Boolean aktivert) {
        String prop = aktivert != null ? aktivert.toString() : "false";
        System.setProperty(FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_AKTIVERT, prop);
    }

    private void setOffset(Period period) {
        String prop = period != null ? period.toString() : "P0D";
        System.setProperty(FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE, prop);
        FPDateUtil.init(); // Laster inn ny konfig verdi i utility klassen
    }

    // kjøres etter hver test
    private void clearOffsetProps() {
        System.clearProperty(FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_AKTIVERT);
        System.clearProperty(FPDateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE);
        FPDateUtil.init(); // Laster inn default offset (0 dager)
    }

}
