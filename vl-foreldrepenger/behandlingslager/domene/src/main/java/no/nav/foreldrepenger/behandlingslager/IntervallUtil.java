package no.nav.foreldrepenger.behandlingslager;

import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.threeten.extra.Interval;

public final class IntervallUtil {

    private IntervallUtil() {
    }

    public static Interval byggIntervall(LocalDate fomDato, LocalDate tomDato) {
        if (tomDato == null) {
            tomDato = TIDENES_ENDE;
        }
        LocalDateTime døgnstart = TIDENES_ENDE.equals(tomDato) ? tomDato.atStartOfDay() : tomDato.atStartOfDay().plusDays(1);
        return Interval.of(
            fomDato.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(),
            døgnstart.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Interval tilIntervall(LocalDate dato) {
        return byggIntervall(dato, dato);
    }
}
