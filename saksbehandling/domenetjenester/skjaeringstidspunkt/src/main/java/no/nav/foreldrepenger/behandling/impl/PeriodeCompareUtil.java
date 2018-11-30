package no.nav.foreldrepenger.behandling.impl;

import java.time.Period;

public final class PeriodeCompareUtil {

    private PeriodeCompareUtil() {
    }

    public static boolean størreEnn(Period period, Period sammenligning) {
        return tilDager(period) > tilDager(sammenligning);
    }

    private static int tilDager(Period period) {
        return period.getDays() + (period.getMonths() * 30) + ((period.getYears() * 12) * 30);
    }
}
