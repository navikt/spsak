package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.utfall;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class KontoUtil {

    private KontoUtil() {
        //hindrer instansiering
    }

    public static LocalDate datoKontoGårTom(FastsettePeriodeGrunnlag grunnlag) {
        UttakPeriode aktuellPeriode = grunnlag.hentPeriodeUnderBehandling();

        int virkedagerTilTom = Integer.MAX_VALUE;
        for (AktivitetIdentifikator aktivitet : grunnlag.getAktiviteter()) {
            int saldo = grunnlag.getTrekkdagertilstand().saldo(aktivitet, aktuellPeriode.getStønadskontotype());
            int virkedager = saldoTilVirkedager(aktuellPeriode, aktivitet, saldo);
            virkedagerTilTom = Math.min(virkedagerTilTom, virkedager);

            if (aktuellPeriode.isFlerbarnsdager()) {
                int saldoFlerbarnsdager = grunnlag.getTrekkdagertilstand().saldo(aktivitet, Stønadskontotype.FLERBARNSDAGER);
                int virkedagerFlerbarnsdager = saldoTilVirkedager(aktuellPeriode, aktivitet, saldoFlerbarnsdager);
                virkedagerTilTom = Math.min(virkedagerFlerbarnsdager, virkedagerTilTom);
            }
        }
        return Virkedager.plusVirkedager(aktuellPeriode.getFom(), virkedagerTilTom);
    }

    private static int saldoTilVirkedager(UttakPeriode periode, AktivitetIdentifikator aktivitet, int saldo) {
        if (periode.harGradering(aktivitet)) {
            if (periode.getGradertArbeidsprosent().compareTo(BigDecimal.valueOf(100)) >= 0) {
                return saldo;
            }
            BigDecimal uttaksprosent = BigDecimal.valueOf(100).subtract(periode.getGradertArbeidsprosent()).divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP);
            return BigDecimal.valueOf(saldo).divide(uttaksprosent, 0, RoundingMode.CEILING).intValue();
        }
        return saldo;
    }


}
