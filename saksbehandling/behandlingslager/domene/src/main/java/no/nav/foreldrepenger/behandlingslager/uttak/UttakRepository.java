package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;

public interface UttakRepository extends BehandlingslagerRepository {

    void lagreOpprinneligUttakResultatPerioder(Behandling behandling, UttakResultatPerioderEntitet uttakResultatEntitet);

    Optional<UttakResultatEntitet> hentUttakResultatHvisEksisterer(Behandling behandling);

    UttakResultatEntitet hentUttakResultat(Behandling behandling);

    void lagreUttaksperiodegrense(Behandling behandling, Uttaksperiodegrense uttaksperiodegrense);

    Optional<Uttaksperiodegrense> hentUttaksperiodegrenseHvisEksisterer(Long behandlingId);

    EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling);

    EndringsresultatSnapshot finnAktivUttakPeriodeGrenseAggregatId(Behandling behandling);
}
