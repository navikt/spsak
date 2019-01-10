package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;

public interface UttakRepository extends BehandlingslagerRepository {

    void lagreOpprinneligUttakResultatPerioder(Behandlingsresultat behandlingsresultat, UttakResultatPerioderEntitet uttakResultatEntitet);

    Optional<UttakResultatEntitet> hentUttakResultatHvisEksisterer(Behandling behandling);

    UttakResultatEntitet hentUttakResultat(Behandling behandling);

    void lagreUttaksperiodegrense(Behandlingsresultat behandlingsresultat1, Uttaksperiodegrense uttaksperiodegrense);

    Optional<Uttaksperiodegrense> hentUttaksperiodegrenseHvisEksisterer(Long behandlingId);

    EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling);

    EndringsresultatSnapshot finnAktivUttakPeriodeGrenseAggregatId(Behandlingsresultat behandlingsresultat);
}
