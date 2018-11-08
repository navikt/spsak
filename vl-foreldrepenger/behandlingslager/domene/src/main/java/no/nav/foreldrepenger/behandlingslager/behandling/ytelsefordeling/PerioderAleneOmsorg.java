package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.List;

public interface PerioderAleneOmsorg extends DokumentasjonPerioder<PeriodeAleneOmsorg> {
    @Override
    List<PeriodeAleneOmsorg> getPerioder();
}
