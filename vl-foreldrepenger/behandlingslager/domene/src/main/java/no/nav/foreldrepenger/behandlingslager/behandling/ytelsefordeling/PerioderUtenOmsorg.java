package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.List;

public interface PerioderUtenOmsorg extends DokumentasjonPerioder<PeriodeUtenOmsorg>{
    @Override
    List<PeriodeUtenOmsorg> getPerioder();
}
