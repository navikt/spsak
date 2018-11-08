package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;

public interface UttakRepository extends BehandlingslagerRepository {

    void lagreOpprinneligUttakResultatPerioder(Behandling behandling, UttakResultatPerioderEntitet uttakResultatEntitet);

    void lagreOverstyrtUttakResultatPerioder(Behandling behandling, UttakResultatPerioderEntitet overstyrtPerioder);

    Optional<UttakResultatEntitet> hentUttakResultatHvisEksisterer(Behandling behandling);

    UttakResultatEntitet hentUttakResultat(Behandling behandling);

    Optional<UttakResultatEntitet> hentUttakResultatPåId (Long id);

    void lagreUttaksperiodegrense(Behandling behandling, Uttaksperiodegrense uttaksperiodegrense);

    Uttaksperiodegrense hentUttaksperiodegrense(Long behandlingId);

    Optional<Uttaksperiodegrense> hentUttaksperiodegrenseHvisEksisterer(Long behandlingId);

    EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling);

    EndringsresultatSnapshot finnAktivUttakPeriodeGrenseAggregatId(Behandling behandling);

    List<OrgManuellÅrsakEntitet> finnOrgManuellÅrsak(String virksomhetsnummer);

    void deaktivterAktivtResultat(Behandling behandling);

    boolean uttakResultatInneholderAvslåttPeriode(Behandling behandling);
}
