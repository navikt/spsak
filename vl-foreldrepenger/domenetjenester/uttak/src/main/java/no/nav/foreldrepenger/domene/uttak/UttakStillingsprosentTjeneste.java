package no.nav.foreldrepenger.domene.uttak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface UttakStillingsprosentTjeneste {
    Optional<BigDecimal> finnStillingsprosentOrdinærtArbeid(Behandling behandling,
                                                            String arbeidsforholdOrgnr,
                                                            String arbeidsforholdId,
                                                            LocalDate dato);

    Optional<BigDecimal> finnStillingsprosentFrilans(Behandling behandling, LocalDate dato);
}
