package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;

public class KontoUtilTest {

    @Test
    public void knekkpunkt_ved_50_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 50, 2);
    }


    @Test
    public void knekkpunkt_ved_80_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 80, 5);
    }

    @Test
    public void knekkpunkt_ved_0_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 0, 1);
    }

    @Test
    public void knekkpunkt_ved_10_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(100, 1, 10, 2);
    }

    @Test
    public void eksempel_1_fra_krav_9() {
        verifiserKnekkpunktVedGradering(17, 4, 60, 10);
    }

    @Test
    public void eksempel_2_fra_krav_9() {
        verifiserKnekkpunktVedGradering(17, 4, 70, 14);
    }

    @Test
    public void knekkpunkt_ved_99punkt99_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(280, 280, new BigDecimal("99.99"), 280 * 10000);
    }

    @Test
    public void knekkpunkt_ved_0punkt01_prosent_arbeid_og_gradering() {
        verifiserKnekkpunktVedGradering(10000, 10000, new BigDecimal("0.01"), 10002);
    }

    @Test
    public void knekkpunkt_ved_gradering_over_eller_lik_100_skal_gi_saldo_som_virkedager_varighet() {
        verifiserKnekkpunktVedGradering(280, 280, new BigDecimal("100.00"), 280);
        verifiserKnekkpunktVedGradering(33, 33, new BigDecimal("120.00"), 33);
    }

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, int arbeidsprosent, int virkedagerVarighet) {
        verifiserKnekkpunktVedGradering(søktOmDag, saldo, BigDecimal.valueOf(arbeidsprosent), virkedagerVarighet);
    }

    private void verifiserKnekkpunktVedGradering(int søktOmDag, int saldo, BigDecimal arbeidsprosent, int virkedagerVarighet) {
        LocalDate idag = LocalDate.now();

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(idag, idag.plusDays(søktOmDag - 1), arbeidsprosent)
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, idag, idag.plusDays(søktOmDag - 1), Collections.singletonList(ARBEIDSFORHOLD_1), arbeidsprosent, PeriodeVurderingType.PERIODE_OK, false, false)
                .medSaldo(Stønadskontotype.MØDREKVOTE, saldo)
                .build();

        LocalDate førsteDatoEtterAtKontoErTom = KontoUtil.datoKontoGårTom(grunnlag);
        Assertions.assertThat(førsteDatoEtterAtKontoErTom).isEqualTo(Virkedager.plusVirkedager(idag, virkedagerVarighet));
    }

}
