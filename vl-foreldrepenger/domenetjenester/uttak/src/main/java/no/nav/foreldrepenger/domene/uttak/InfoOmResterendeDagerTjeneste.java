package no.nav.foreldrepenger.domene.uttak;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface InfoOmResterendeDagerTjeneste {
    Integer getDisponibleDager(Behandling behandling, Boolean aleneOmsorg, Boolean annenForelderHarRett);

    Integer getDisponibleFellesDager(Behandling behandling);

    Optional<LocalDate> getSisteDagAvSistePeriodeTilAnnenForelder(Behandling behandling);
}
