package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Frilansoppdrag {

    DatoIntervallEntitet getPeriode();

    String getOppdragsgiver();
}
