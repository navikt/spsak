package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.math.BigDecimal;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.typer.Beløp;

public class YtelseStørrelseBuilder {
    private final YtelseStørrelseEntitet ytelseStørrelseEntitet;

    YtelseStørrelseBuilder(YtelseStørrelseEntitet ytelseStørrelseEntitet) {
        this.ytelseStørrelseEntitet = ytelseStørrelseEntitet;
    }

    public static YtelseStørrelseBuilder ny() {
        return new YtelseStørrelseBuilder(new YtelseStørrelseEntitet());
    }

    public YtelseStørrelseBuilder medVirksomhet(Virksomhet virksomhet) {
        this.ytelseStørrelseEntitet.setVirksomhet(virksomhet);
        return this;
    }

    public YtelseStørrelseBuilder medBeløp(BigDecimal verdi) {
        BigDecimal verdiNotNull = verdi != null ? verdi : new BigDecimal(0);
        this.ytelseStørrelseEntitet.setBeløp(new Beløp(verdiNotNull));
        return this;
    }

    public YtelseStørrelseBuilder medHyppighet(InntektPeriodeType frekvens) {
        this.ytelseStørrelseEntitet.setHyppighet(frekvens);
        return this;
    }
    public YtelseStørrelse build() {
        if (ytelseStørrelseEntitet.hasValues()) {
            return ytelseStørrelseEntitet;
        }
        throw new IllegalStateException();
    }

}
