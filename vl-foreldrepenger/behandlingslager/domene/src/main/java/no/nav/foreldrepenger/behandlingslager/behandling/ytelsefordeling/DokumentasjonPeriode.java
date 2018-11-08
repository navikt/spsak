package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface DokumentasjonPeriode {
    DatoIntervallEntitet getPeriode();

    UttakDokumentasjonType getDokumentasjonType();
}
