package no.nav.foreldrepenger.domene.mottak.forsendelse;

import java.util.Arrays;

public enum ForsendelseStatus {
    INNVLIGET("behandling_innvilget"),
    AVSLÅTT("behandling_avslått"),
    PÅGÅR("behandling_pågar"),
    PÅ_VENT("behandling_på_vent");

    private final String value;

    ForsendelseStatus(String value) {
        this.value = value;
    }

    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static ForsendelseStatus asEnumValue(String s) {
        return Arrays.stream(values())
            .filter(v -> v.value.equals(s))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("ugyldig verdi: " + s));
    }
}
