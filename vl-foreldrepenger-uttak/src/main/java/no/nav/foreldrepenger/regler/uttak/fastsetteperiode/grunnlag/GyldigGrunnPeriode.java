package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;


public class GyldigGrunnPeriode extends LukketPeriode {
    public GyldigGrunnPeriode(LocalDate fom, LocalDate tom) {
        super(fom, tom);
    }
}
