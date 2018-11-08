package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

public class PeriodeMedAleneomsorg extends Periode {
    public PeriodeMedAleneomsorg(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }
}
