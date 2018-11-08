package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

class PeriodeUtenOmsorg extends LukketPeriode {
    public PeriodeUtenOmsorg(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }
}
