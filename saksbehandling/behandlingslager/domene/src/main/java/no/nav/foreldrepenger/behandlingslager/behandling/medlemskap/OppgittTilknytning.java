package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.time.LocalDate;
import java.util.Set;

public interface OppgittTilknytning {

    boolean isOppholdNå();

    LocalDate getOppgittDato();

    Set<OppgittLandOpphold> getOpphold();

    boolean isOppholdINorgeSistePeriode();

    boolean isOppholdINorgeNestePeriode();
}
