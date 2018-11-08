package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;

public interface UttakBeregningsandelTjeneste {

    /**
     * Hent beregningsandeler fra behandlingsgrunnlag.
     *
     * @param behandling behandlingen som det skal hentes behandlingsgrunnlag fra.
     *
     * @return gir en listen av andeler dersom beregningsgrunnlaget er laget.
     */
    List<BeregningsgrunnlagPrStatusOgAndel> hentAndeler(Behandling behandling);

}
