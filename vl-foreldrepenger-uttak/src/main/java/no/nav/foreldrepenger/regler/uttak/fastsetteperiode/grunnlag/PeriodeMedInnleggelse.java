package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;

public class PeriodeMedInnleggelse extends LukketPeriode{
    public PeriodeMedInnleggelse(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }
}
