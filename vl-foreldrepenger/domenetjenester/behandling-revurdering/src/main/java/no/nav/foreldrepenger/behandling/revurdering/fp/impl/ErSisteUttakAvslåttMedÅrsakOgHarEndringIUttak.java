package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;

class ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak {
    private ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak() {}

    public static boolean vurder(Optional<UttakResultatEntitet> uttakresultatOpt, boolean erEndringIUttakFraEndringstidspunkt) {
        return erEndringIUttakFraEndringstidspunkt && kontrollerErSisteUttakAvslåttMedÅrsak(uttakresultatOpt);
    }

    public static boolean kontrollerErSisteUttakAvslåttMedÅrsak(Optional<UttakResultatEntitet> uttakresultatOpt) {
        if (!uttakresultatOpt.isPresent()) {
            return false;
        }
        Set<PeriodeResultatÅrsak> opphørsAvslagÅrsaker = IkkeOppfyltÅrsak.opphørsAvslagÅrsaker();
        UttakResultatEntitet uttakresultat = uttakresultatOpt.get();
        return opphørsAvslagÅrsaker.contains(finnSisteUttaksperiode(uttakresultat).getPeriodeResultatÅrsak());
    }

    private static UttakResultatPeriodeEntitet finnSisteUttaksperiode(UttakResultatEntitet uttak) {
        List<UttakResultatPeriodeEntitet> perioder = uttak.getGjeldendePerioder().getPerioder();
        perioder.sort(Comparator.comparing(UttakResultatPeriodeEntitet::getFom).reversed());
        return perioder.get(0);
    }

    public static Behandlingsresultat fastsett(Behandling revurdering) {
        return SettOpphørOgIkkeRett.fastsett(revurdering);
    }
}
