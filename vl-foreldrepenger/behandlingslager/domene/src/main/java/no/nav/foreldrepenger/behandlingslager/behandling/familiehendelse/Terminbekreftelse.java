package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;

public interface Terminbekreftelse {

    LocalDate getTermindato();

    LocalDate getUtstedtdato();

    String getNavnPå();

}
