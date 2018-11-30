package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.List;

public interface AktørInntekt {

    /**
     * Aktøren inntekten er relevant for
     * @return aktørid
     */
    AktørId getAktørId();

    /**
     * Inntekter fra SIGRUN. Inneholder kun beregnet skatt
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getBeregnetSkatt();

    /**
     * Inntekter fra inntektskomponenten. Inneholder kun pensjonsgivende inntekt
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getInntektPensjonsgivende();

    /**
     * Inntekter fra inntektskomponenten. Inneholder kun inntekter som er relevant for beregningsgrunnlaget
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getInntektBeregningsgrunnlag();

    /**
     * Inntekter fra inntektskomponenten. Inneholder kun inntekter som er relevant for sammenligningsgrunnlaget
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getInntektSammenligningsgrunnlag();
}
