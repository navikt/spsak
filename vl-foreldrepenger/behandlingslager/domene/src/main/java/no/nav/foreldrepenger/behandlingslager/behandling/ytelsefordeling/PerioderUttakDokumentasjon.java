package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface PerioderUttakDokumentasjon extends DokumentasjonPerioder{
    @Override
    List<PeriodeUttakDokumentasjon> getPerioder();
}
