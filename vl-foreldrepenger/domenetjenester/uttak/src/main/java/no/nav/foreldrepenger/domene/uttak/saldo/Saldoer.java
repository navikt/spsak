package no.nav.foreldrepenger.domene.uttak.saldo;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;

public interface Saldoer {

    /**
     * Saldo for angitt stønadskonto og aktivitet.
     *
     * @param stønadskonto angitt stønadskonto.
     * @param aktivitet angitt aktivitet.
     *
     * @return antall gjenstående dager for angitt stønadskonto og aktivitet.
     */
    int saldo(StønadskontoType stønadskonto, Aktivitet aktivitet);

    /**
     * Saldo for angitt stønadskonto. Dersom saldo der forskjellige på aktivitetene, så blir største saldo valgt.
     *
     * @param stønadskonto angitt stønadskonto.
     *
     * @return antall gjenstående dager for angitt stønadskonto.
     */
    int saldo(StønadskontoType stønadskonto);

    /**
     * Finn alle aktiviteter som det er trukket dager for søker.
     *
     * @return ett sett av aktiviteter.
     */
    Set<Aktivitet> aktiviteterForSøker();

    /**
     * Hvilke stønadskontoer er opprettet.
     *
     * @return et sett med stønadskontotyper.
     */
    Set<StønadskontoType> stønadskontoer();

    /**
     * Max antall dager på angitt stønadskonto.
     *
     * @param stønadskonto angitt stønadskonto.
     *
     * @return antall dager som max kan brukes av denne stønadstypen. 0 dersom stønadskonto ikke er tilgjengelig for
     *         denne behandlingen.
     */
    int getMaxDager(StønadskontoType stønadskonto);

    /**
     * Siste mulig uttaksdato for behandlingen gitt gjenstående dager på saldo.
     *
     * @return siste mulige uttaksdato. Kan være utilgjengelig dersom det ikke finnes en uttaksplan.
     */
    Optional<LocalDate> getMaksDatoUttak();
}
