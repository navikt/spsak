package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface FastsettUttaksgrunnlagTjeneste {

    /**
     * Fastsett uttaksgrunnlaget for en gitt behandling.
     *
     * @param behandling behandlingen som det skal fastsettes uttaksgrunnlaget for.
     */
    void fastsettUttaksgrunnlag(Behandling behandling);

}
