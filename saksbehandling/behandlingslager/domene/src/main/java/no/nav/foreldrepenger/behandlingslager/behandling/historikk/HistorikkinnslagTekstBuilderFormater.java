package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import no.nav.fpsak.tidsserie.LocalDateInterval;

public final class HistorikkinnslagTekstBuilderFormater {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    private HistorikkinnslagTekstBuilderFormater() {
    }

    public static <T> String formatString(T verdi) {
        if (verdi == null) {
            return null;
        }
        if (verdi instanceof LocalDate) {
            LocalDate localDate = (LocalDate) verdi;
            return formatDate(localDate);
        }
        if (verdi instanceof LocalDateInterval) {
            LocalDateInterval interval = (LocalDateInterval) verdi;
            return formatDate(interval.getFomDato()) + " - " + formatDate(interval.getTomDato());
        }
        return verdi.toString();
    }

    private static String formatDate(LocalDate localDate) {
        return DATE_FORMATTER.format(localDate);
    }
}
