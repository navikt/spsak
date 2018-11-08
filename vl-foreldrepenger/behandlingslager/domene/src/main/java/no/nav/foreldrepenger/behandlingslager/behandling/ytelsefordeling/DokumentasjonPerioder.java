package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.List;

public interface DokumentasjonPerioder<T extends DokumentasjonPeriode> {
    List<T> getPerioder();
}
