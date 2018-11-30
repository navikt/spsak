package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface AnnenAktivitet {

    ArbeidType getArbeidType();

    DatoIntervallEntitet getPeriode();
}
