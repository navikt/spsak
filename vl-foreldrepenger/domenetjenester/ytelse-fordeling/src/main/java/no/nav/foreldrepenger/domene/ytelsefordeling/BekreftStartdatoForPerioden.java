package no.nav.foreldrepenger.domene.ytelsefordeling;

import java.time.LocalDate;

public class BekreftStartdatoForPerioden {

    private final LocalDate startdatoForPerioden;

    public BekreftStartdatoForPerioden(LocalDate startdatoForPerioden) {
        this.startdatoForPerioden = startdatoForPerioden;
    }

    public LocalDate getStartdatoForPerioden() {
        return startdatoForPerioden;
    }
}
