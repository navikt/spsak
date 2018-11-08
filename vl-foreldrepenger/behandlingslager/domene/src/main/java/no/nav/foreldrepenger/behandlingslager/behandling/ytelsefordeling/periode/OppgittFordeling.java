package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode;

import java.util.List;

public interface OppgittFordeling {

    List<OppgittPeriode> getOppgittePerioder();

    boolean getErAnnenForelderInformert();
}
