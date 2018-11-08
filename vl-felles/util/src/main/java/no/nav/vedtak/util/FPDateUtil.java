package no.nav.vedtak.util;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import no.nav.vedtak.konfig.PropertyUtil;

/** Returner funksjonelt tidsoffset, Brukes med LocalDate og LocalDateTime. eks. LocalDate.now(FPDateUtil.getOffset) */
public class FPDateUtil {
    private static volatile ClockProvider clockProvider;

    /** Bare for enhetstest */
    public static void init() {
        init(new SystemConfiguredClockProvider());
    }

    /** Bare for enhetstest */
    public static void init(ClockProvider provider) {
        clockProvider = provider;
    }

    /** Returnerer nåværende ClockProvider, eller null dersom aldri initialisert. */
    public static ClockProvider getCurrentClockProvider() {
        return clockProvider;
    }

    public static Clock getOffset() {
        if (clockProvider == null) {
            init();
        }
        return clockProvider.getClock();
    }

    public static LocalDate iDag() {
        return LocalDate.now(getOffset());
    }

    public static LocalDateTime nå() {
        return LocalDateTime.now(getOffset());
    }

    /** Interface for å kunne tilby en egendefinert Clock. */
    public interface ClockProvider {
        Clock getClock();
    }

    /**
     * System clock, med optional offset aktivert og duration i tid.
     * Konfigurasjon:
     *      funksjonelt.tidsoffset.aktivert (boolean) angir om tidsjustering skal brukes - parameter settes i .conf fil
     *      funksjonelt.tidsoffset.offset (Duration) angir offset i Duration format f.eks. P-2D / P2D (2 dager bakover/frem) - parameter settes i databasen
     **/
    public static class SystemConfiguredClockProvider implements ClockProvider {
        public static final String PROPERTY_KEY_OFFSET_AKTIVERT = "funksjonelt.tidsoffset.aktivert";
        public static final String PROPERTY_KEY_OFFSET_PERIODE = "funksjonelt.tidsoffset.offset";

        private final Boolean offsetAktivert;
        private final Period offsetPeriod;
        private volatile Clock clock;

        public SystemConfiguredClockProvider() {
            String offsetAktivertStr = PropertyUtil.getProperty(PROPERTY_KEY_OFFSET_AKTIVERT);
            this.offsetAktivert = Boolean.parseBoolean(offsetAktivertStr);
            if (this.offsetAktivert) {
                String offsetPeriode = PropertyUtil.getProperty(PROPERTY_KEY_OFFSET_PERIODE);
                if (offsetAktivertStr == null || offsetPeriode.isEmpty()) {
                    throw new IllegalStateException("Miljøfeil: tidsoffset er angitt aktivert men mangler offset verdi [" + PROPERTY_KEY_OFFSET_PERIODE + "]");
                }
                this.offsetPeriod = Period.parse(offsetPeriode);
            } else {
                this.offsetPeriod = Period.ofDays(0);
            }
            initClock(offsetPeriod);
        }

        private void initClock(Period period) {
            Duration duration = Duration.ofDays(offsetDager(period));
            if (this.offsetAktivert) {
                this.clock = Clock.offset(Clock.systemDefaultZone(), duration != null ? duration : Duration.ofDays(0));
            } else {
                this.clock = Clock.systemDefaultZone();
            }
        }

        @Override
        public Clock getClock() {
            return this.clock;
        }

        private Long offsetDager(Period period) {
            LocalDate realDate = LocalDate.now();
            LocalDate funcDate = periodToLocalDate(period);
            return ChronoUnit.DAYS.between(realDate, funcDate);
        }

        private LocalDate periodToLocalDate(Period period) {
            LocalDate date = LocalDate.now();
            if (period == null) {
                return date;
            }
            return date.plusYears(period.getYears())
                    .plusMonths(period.getMonths())
                    .plusDays(period.getDays());
        }
    }
}
