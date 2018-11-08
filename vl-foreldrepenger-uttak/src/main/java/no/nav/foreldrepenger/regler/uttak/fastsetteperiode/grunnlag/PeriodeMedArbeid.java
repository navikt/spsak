package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class PeriodeMedArbeid extends LukketPeriode {
    public PeriodeMedArbeid(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }
}
