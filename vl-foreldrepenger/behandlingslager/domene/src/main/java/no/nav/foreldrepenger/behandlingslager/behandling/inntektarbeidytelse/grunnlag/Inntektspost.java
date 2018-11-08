package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.domene.typer.Beløp;

public interface Inntektspost {

    /**
     * Underkategori av utbetaling
     * <p>
     * F.eks
     * <ul>
     * <li>Lønn</li>
     * <li>Ytelse</li>
     * <li>Næringsinntekt</li>
     * </ul>
     *
     * @return {@link InntektspostType}
     */
    InntektspostType getInntektspostType();

    /**
     * Periode start
     *
     * @return første dag i perioden
     */
    LocalDate getFraOgMed();

    /**
     * Periode slutt
     *
     * @return siste dag i perioden
     */
    LocalDate getTilOgMed();

    /**
     * Beløpet som har blitt utbetalt i perioden
     *
     * @return Beløpet
     */
    Beløp getBeløp();

    YtelseType getYtelseType();
}
