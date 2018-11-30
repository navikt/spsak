package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.math.BigDecimal;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class YtelseAnvistBuilder {
    private final YtelseAnvistEntitet ytelseAnvistEntitet;

    YtelseAnvistBuilder(YtelseAnvistEntitet ytelseAnvistEntitet) {
        this.ytelseAnvistEntitet = ytelseAnvistEntitet;
    }

    static YtelseAnvistBuilder ny() {
        return new YtelseAnvistBuilder(new YtelseAnvistEntitet());
    }

    public YtelseAnvistBuilder medBeløp(BigDecimal beløp) {
        this.ytelseAnvistEntitet.setBeløp(beløp);
        return this;
    }

    public YtelseAnvistBuilder medDagsats(BigDecimal dagsats) {
        this.ytelseAnvistEntitet.setDagsats(dagsats);
        return this;
    }

    public YtelseAnvistBuilder medAnvistPeriode(DatoIntervallEntitet intervallEntitet){
        this.ytelseAnvistEntitet.setAnvistPeriode(intervallEntitet);
        return this;
    }

    public YtelseAnvistBuilder medUtbetalingsgradProsent(BigDecimal utbetalingsgradProsent) {
        this.ytelseAnvistEntitet.setUtbetalingsgradProsent(utbetalingsgradProsent);
        return this;
    }

    public YtelseAnvist build() {
        return ytelseAnvistEntitet;
    }

}
