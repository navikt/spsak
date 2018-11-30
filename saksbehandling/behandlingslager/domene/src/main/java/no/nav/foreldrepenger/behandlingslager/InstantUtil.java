package no.nav.foreldrepenger.behandlingslager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class InstantUtil {

    private InstantUtil() {
    }

    public static LocalDateTime tilLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static LocalDate tilLocalDate(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }
}
