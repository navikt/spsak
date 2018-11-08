package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class UttakPeriodeAktivitet {
    private Stønadskontotype stønadskontotype;
    private AktivitetIdentifikator aktivitetIdentifikator;
    private BigDecimal gradertArbeidsprosent = BigDecimal.ZERO;
    private int trekkdager;
    private BigDecimal utbetalingsgrad;

    public UttakPeriodeAktivitet(AktivitetIdentifikator aktivitetIdentifikator, Stønadskontotype stønadskontotype,
                                 int trekkdager, BigDecimal utbetalingsgrad) {
        Objects.requireNonNull(aktivitetIdentifikator);
        Objects.requireNonNull(stønadskontotype);
        Objects.requireNonNull(trekkdager);
        Objects.requireNonNull(utbetalingsgrad);

        this.aktivitetIdentifikator = aktivitetIdentifikator;
        this.stønadskontotype = stønadskontotype;
        this.trekkdager = trekkdager;
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public UttakPeriodeAktivitet(AktivitetIdentifikator aktivitetIdentifikator,
                                 Stønadskontotype stønadskontotype,
                                 int trekkdager,
                                 BigDecimal utbetalingsgrad,
                                 BigDecimal gradertArbeidsprosent) {
        Objects.requireNonNull(aktivitetIdentifikator);
        Objects.requireNonNull(stønadskontotype);
        Objects.requireNonNull(trekkdager);
        Objects.requireNonNull(utbetalingsgrad);
        Objects.requireNonNull(gradertArbeidsprosent);

        this.aktivitetIdentifikator = aktivitetIdentifikator;
        this.stønadskontotype = stønadskontotype;
        this.trekkdager = trekkdager;
        this.utbetalingsgrad = utbetalingsgrad;
        this.gradertArbeidsprosent = gradertArbeidsprosent;
    }

    public AktivitetIdentifikator getAktivitetIdentifikator() {
        return aktivitetIdentifikator;
    }

    public BigDecimal getGradertArbeidsprosent() {
        return gradertArbeidsprosent;
    }

    public int getTrekkdager() {
        return trekkdager;
    }

    public Stønadskontotype getStønadskontotype() {
        return stønadskontotype;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

}
