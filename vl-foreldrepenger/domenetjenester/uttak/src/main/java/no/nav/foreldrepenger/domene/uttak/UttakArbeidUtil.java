package no.nav.foreldrepenger.domene.uttak;

import java.math.BigDecimal;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;

public final class UttakArbeidUtil {

    private UttakArbeidUtil() {
    }

    public static BigDecimal hentStillingsprosent(AktivitetsAvtale aktivitetsAvtale) {
        return aktivitetsAvtale.getProsentsats() == null ? BigDecimal.valueOf(100) : aktivitetsAvtale.getProsentsats().getVerdi();
    }
}
