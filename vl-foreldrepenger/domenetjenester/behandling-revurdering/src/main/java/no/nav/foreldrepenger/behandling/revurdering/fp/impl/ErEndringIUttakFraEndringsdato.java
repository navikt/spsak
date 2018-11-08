package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;

class ErEndringIUttakFraEndringsdato {
    private ErEndringIUttakFraEndringsdato() {}

    public static boolean vurder(LocalDate endringsdato, Optional<UttakResultatEntitet> uttakresultatRevurderingOpt, Optional<UttakResultatEntitet> uttakresultatOriginalOpt) {
        List<UttakResultatPeriodeEntitet> uttaksPerioderEtterEndringTP = finnUttaksperioderEtterEndringsdato(endringsdato, uttakresultatRevurderingOpt);
        List<UttakResultatPeriodeEntitet> originaleUttaksPerioderEtterEndringTP = finnUttaksperioderEtterEndringsdato(endringsdato, uttakresultatOriginalOpt);
        return !erUttakresultatperiodeneLike(uttaksPerioderEtterEndringTP, originaleUttaksPerioderEtterEndringTP);
    }

    static List<UttakResultatPeriodeEntitet> finnUttaksperioderEtterEndringsdato(LocalDate endringsdato, Optional<UttakResultatEntitet> uttakResultatEntitet) {
        if (!uttakResultatEntitet.isPresent()) {
            return Collections.emptyList();
        }
        UttakResultatEntitet uttakresultat = uttakResultatEntitet.get();
        return uttakresultat.getGjeldendePerioder().getPerioder()
            .stream().filter(periode -> !periode.getFom().isBefore(endringsdato)).collect(Collectors.toList());
    }

    private static boolean erAktiviteteneIPeriodeneLike(UttakResultatPeriodeEntitet periode1, UttakResultatPeriodeEntitet periode2) {
        List<UttakResultatPeriodeAktivitetEntitet> aktiviteter1 = periode1.getAktiviteter();
        List<UttakResultatPeriodeAktivitetEntitet> aktiviteter2 = periode2.getAktiviteter();
        if (aktiviteter1.size() != aktiviteter2.size()) {
            return false;
        }
        int antallAktiviteter = aktiviteter1.size();
        for (int i = 0; i < antallAktiviteter; i++) {
            // Sjekk på Trekk i antall uker/dager
            UttakResultatPeriodeAktivitetEntitet aktivitet1 = aktiviteter1.get(i);
            UttakResultatPeriodeAktivitetEntitet aktivitet2 = aktiviteter2.get(i);
            if (aktivitet1.getTrekkdager() != aktivitet2.getTrekkdager()) {
                return false;
            }
            if (!Objects.equals(aktivitet1.getTrekkdager(), aktivitet2.getTrekkdager())
                || Objects.equals(aktivitet1.getArbeidsprosent(), aktivitet2.getArbeidsprosent())
                || Objects.equals(aktivitet1.getUtbetalingsprosent(), aktivitet2.getUtbetalingsprosent())
                || Objects.equals(aktivitet1.getTrekkonto(), aktivitet2.getTrekkonto()))
            // Sjekk på Stønadskonto
            if (!aktivitet1.getTrekkonto().equals(aktivitet2.getTrekkonto())) {
                return false;
            }
            // Sjekk på Andel i arbeid
            if ((aktivitet1.getArbeidsprosent()
                .compareTo(aktivitet2.getArbeidsprosent())) != 0) {
                return false;
            }
            // Sjekk på Utbetalingsgrad
            if ((aktivitet1.getUtbetalingsprosent()
                .compareTo(aktivitet2.getUtbetalingsprosent())) != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean erUttakresultatperiodeneLike(List<UttakResultatPeriodeEntitet> listeMedPerioder1, List<UttakResultatPeriodeEntitet> listeMedPerioder2) {
        // Sjekk på Ny/fjernet
        if (listeMedPerioder1.size() != listeMedPerioder2.size()) {
            return false;
        }
        int antallPerioder = listeMedPerioder1.size();
        for (int i = 0; i < antallPerioder; i++) {
            UttakResultatPeriodeEntitet periode1 = listeMedPerioder1.get(i);
            UttakResultatPeriodeEntitet periode2 = listeMedPerioder2.get(i);
            if (!periode1.getFom().isEqual(periode2.getFom()) || !periode1.getTom().isEqual(periode2.getTom())) {
                return false;
            }
            if (!erAktiviteteneIPeriodeneLike(periode1, periode2)) {
                return false;
            }
            // Sjekk på Samtidig uttak
            if (periode1.isSamtidigUttak() != periode2.isSamtidigUttak()) {
                return false;
            }
            if (periode1.isFlerbarnsdager() != periode2.isFlerbarnsdager()) {
                return false;
            }
            // Sjekk på Utfall
            if (!periode1.getPeriodeResultatType().equals(periode2.getPeriodeResultatType())) {
                return false;
            }
            // Sjekk på Gradering utfall
            if (periode1.isGraderingInnvilget() != periode2.isGraderingInnvilget()) {
                return false;
            }
        }
        return true;
    }
}
