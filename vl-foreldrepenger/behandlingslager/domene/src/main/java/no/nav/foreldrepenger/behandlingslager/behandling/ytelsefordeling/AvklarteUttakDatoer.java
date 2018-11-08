package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.time.LocalDate;

public interface AvklarteUttakDatoer {
    LocalDate getFørsteUttaksDato();

    LocalDate getEndringsdato();
}
