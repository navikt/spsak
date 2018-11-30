package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.domene.typer.Beløp;


public interface YtelseAnvist {

    LocalDate getAnvistFOM();

    LocalDate getAnvistTOM();

    Optional<Beløp> getBeløp();

    Optional<BigDecimal> getDagsats();

    Optional<BigDecimal> getUtbetalingsgradProsent();
}
