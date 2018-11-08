package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;

public interface Permisjon {

    /**
     * Beskrivelse av permisjonen
     *
     * @return {@link PermisjonsbeskrivelseType}
     */
    PermisjonsbeskrivelseType getPermisjonsbeskrivelseType();

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
     * Prosentsats som aktøren er permitert fra arbeidet
     *
     * @return prosentsats
     */
    Stillingsprosent getProsentsats();
}
