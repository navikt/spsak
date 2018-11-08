package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;

class FastsettePerioderUtil {

    private static final BigDecimal HUNDRE = BigDecimal.valueOf(100L);

    private FastsettePerioderUtil() {
        //For å hindre instanser
    }

    static BigDecimal finnArbeidstidsprosentFraPermisjonPeriode(Permisjon permisjon, BigDecimal stillingsprosent) {
        //Sørg for at permisjonprosent aldri er mer en 100%.
        BigDecimal permisjonsprosent = permisjon.getProsentsats().getVerdi();
        if (permisjonsprosent.compareTo(HUNDRE) > 0) {
            permisjonsprosent = HUNDRE;
        }

        //stillingsprosent * (100 - permisjonsprosent) / 100
        return stillingsprosent
            .multiply(HUNDRE.subtract(permisjonsprosent))
            .divide(HUNDRE, 2, RoundingMode.HALF_UP);
    }
}
