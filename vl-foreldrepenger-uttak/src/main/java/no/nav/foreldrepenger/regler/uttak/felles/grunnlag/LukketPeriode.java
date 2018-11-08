package no.nav.foreldrepenger.regler.uttak.felles.grunnlag;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;

/**
 * En periode som har definert både start- og slutt-tidpunkt
 */
public class LukketPeriode extends Periode {

    public LukketPeriode(LocalDate fom, LocalDate tom) {
        super(fom, tom);
        Objects.requireNonNull(tom);
        Objects.requireNonNull(fom);
    }

    public int virkedager() {
        return Virkedager.beregnAntallVirkedager(this);
    }

}
