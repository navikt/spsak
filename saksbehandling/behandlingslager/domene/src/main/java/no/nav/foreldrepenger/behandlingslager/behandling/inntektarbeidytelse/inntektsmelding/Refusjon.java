package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.typer.Beløp;

public interface Refusjon {

    Beløp getRefusjonsbeløp();
    LocalDate getFom();
}
