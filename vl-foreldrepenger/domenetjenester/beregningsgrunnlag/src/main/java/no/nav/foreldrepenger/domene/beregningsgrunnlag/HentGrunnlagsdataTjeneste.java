package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;

public interface HentGrunnlagsdataTjeneste {

    boolean vurderOmNyesteGrunnlagsdataSkalHentes(Behandling behandling);

    void innhentInntektsInformasjonBeregningOgSammenligning(Behandling behandling);

    /**
     * Henter ut siste gjeldende beregningsgrunnlag for gitt behandling eller forrige behandling (f. eks om det er
     * en revurdering). Et gjeldene beregningsgrunnlag er ferdig beregnet og skal da være avkortet og redusert.
     * @return Optional med gjeldene BG. Tom optional om ikke eksisterer.
     */
    Optional<Beregningsgrunnlag> hentGjeldendeBeregningsgrunnlag(Behandling behandling);

    boolean brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(Behandling behandling);
}
