package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface NaturalYtelse {

    DatoIntervallEntitet getPeriode();

    Beløp getBeloepPerMnd();

    NaturalYtelseType getType();
}
