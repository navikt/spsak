package no.nav.foreldrepenger.domene.uttak.uttaksplan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;

public interface BeregnUttaksaldoTjeneste {

    /**
     * Beregner totalt antall disponible dager etter å ha trukket fra uttaket i gjeldende behandling og relatert behandling.
     *
     * @param behandling
     * @return Totalt antall disponible dager
     */
    Optional<Integer> beregnDisponibleDager(Behandling behandling);


    /**
     * Finner perioder som overlapper med fom og tom dato der det ikke er huket av for at det skal være samtidig uttak.
     *
     * @param fom
     * @param tom
     * @param perioder
     * @return Liste med perioder som overlapper fom og tom dato
     */
    List<UttakResultatPeriodeEntitet> finnOverlappendePerioderUtenSamtidigUttak(LocalDate fom, LocalDate tom, List<UttakResultatPeriodeEntitet> perioder);

    boolean erInnvilgetEllerAvslåttMedTrekkdager(UttakResultatPeriodeEntitet uttakResultatPeriode);

    /**
     *
     * Gitt en aktivitet, og det finnes overlappende perioder, finn antall trekkdager der det ikke er overlapp.
     *
     * @param aktivititet
     * @param overlappendePerioder
     * @return antall trekkdager
     */
    int finnAntallTrekkdagerUtenOverlapp(UttakResultatPeriodeAktivitetEntitet aktivititet, List<UttakResultatPeriodeEntitet> overlappendePerioder);
}
