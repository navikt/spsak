package no.nav.foreldrepenger.domene.uttak;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface OpphørFPTjeneste {
    Optional<LocalDate> getFørsteStønadsDato(Behandling behandling);
    Optional<LocalDate> getOpphørsdato(Behandling behandling);
}
