package no.nav.foreldrepenger.domene.uttak.saldo.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.domene.uttak.saldo.Aktivitet;

public class SaldoerImplTest {

    private static Aktivitet AKTIVITET1_SØKER = new Aktivitet(UttakArbeidType.ORDINÆRT_ARBEID, "111", "222");
    private static Aktivitet AKTIVITET2_SØKER = new Aktivitet(UttakArbeidType.ORDINÆRT_ARBEID, "333", "444");
    private static Aktivitet AKTIVITET1_MOTPART = new Aktivitet(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE, "555", "666");
    private static Aktivitet AKTIVITET2_MOTPART = new Aktivitet(UttakArbeidType.ORDINÆRT_ARBEID, "777", "888");

    private SaldoerImpl saldoer = new SaldoerImpl();

    @Before
    public void oppsett() {
        saldoer = new SaldoerImpl();
    }

    @Test
    public void ingen_max_dag_og_ingen_trekkdager_skal_alltid_gi_0_i_saldo() {
       assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(0);
    }

    @Test
    public void max_dager_minus_trekkdag_skal_bli_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 67);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(100 - 67);
    }

    @Test
    public void flere_trekk_gir_riktig_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 20);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 25);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(100 - 20 - 25);
    }


    @Test
    public void for_stort_trekk_gir_riktig_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 110);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE)).isEqualTo(-10);
        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(-10);
    }


    @Test
    public void for_stort_trekk_på_flere_aktivitetr_gir_riktig_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 120);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER, 110);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE)).isEqualTo(-10);
        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(-20);
        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(-10);
    }


    @Test
    public void flere_trekk_på_forskjellig_aktivitet_gir_forskjellig_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 20);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER, 25);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(100 - 20);
        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(100 - 25);
    }

    @Test
    public void trekkdager_på_annen_part_skal_telle_med_i_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 20);
        saldoer.trekkForAnnenPart(StønadskontoType.FELLESPERIODE, AKTIVITET1_MOTPART, 25);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(100 - 20 - 25);
    }

    @Test
    public void minste_trekkdager_på_annen_part_skal_telle_med_i_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 20);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER, 30);
        saldoer.trekkForAnnenPart(StønadskontoType.FELLESPERIODE, AKTIVITET1_MOTPART, 10);
        saldoer.trekkForAnnenPart(StønadskontoType.FELLESPERIODE, AKTIVITET2_MOTPART, 25);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(100 - 20 - 10);
        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(100 - 30 - 10);
    }

    @Test
    public void flere_trekk_på_annen_part_skal_telle_med_i_saldo() {
        saldoer.setMaxDager(StønadskontoType.FELLESPERIODE, 100);
        saldoer.setMaxDager(StønadskontoType.FEDREKVOTE, 60);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER, 20);
        saldoer.trekkForSøker(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER, 30);
        saldoer.trekkForAnnenPart(StønadskontoType.FELLESPERIODE, AKTIVITET1_MOTPART, 10);
        saldoer.trekkForAnnenPart(StønadskontoType.FELLESPERIODE, AKTIVITET1_MOTPART, 10);
        saldoer.trekkForAnnenPart(StønadskontoType.FELLESPERIODE, AKTIVITET2_MOTPART, 25);
        saldoer.trekkForAnnenPart(StønadskontoType.FEDREKVOTE, AKTIVITET1_MOTPART, 20);
        saldoer.trekkForAnnenPart(StønadskontoType.FEDREKVOTE, AKTIVITET2_MOTPART, 10);

        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET1_SØKER)).isEqualTo(100 - 20 - 20);
        assertThat(saldoer.saldo(StønadskontoType.FELLESPERIODE, AKTIVITET2_SØKER)).isEqualTo(100 - 30 - 20);
        assertThat(saldoer.saldo(StønadskontoType.FEDREKVOTE, AKTIVITET1_SØKER)).isEqualTo(60 - 10);
        assertThat(saldoer.saldo(StønadskontoType.FEDREKVOTE, AKTIVITET2_SØKER)).isEqualTo(60 - 10);
    }

    @Test
    public void kan_sett_og_hente_siste_uttaksdato() {
        saldoer.setMaksDatoUttak(Optional.of(LocalDate.of(2018,1,1)));

        Optional<LocalDate> sisteUttaksdato = saldoer.getMaksDatoUttak();

        assertThat(sisteUttaksdato.get()).isEqualTo(LocalDate.of(2018,1,1));
    }

}
