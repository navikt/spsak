package no.nav.foreldrepenger.beregningsgrunnlag;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;

public class BeregningsgrunnlagRegelResultat {
    private Beregningsgrunnlag beregningsgrunnlag;
    private List<AksjonspunktDefinisjon> aksjonspunkter;

    public BeregningsgrunnlagRegelResultat(Beregningsgrunnlag beregningsgrunnlag, List<AksjonspunktDefinisjon> aksjonspunktDefinisjoner) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.aksjonspunkter = aksjonspunktDefinisjoner;
    }

    public Beregningsgrunnlag getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public List<AksjonspunktDefinisjon> getAksjonspunkter() {
        return aksjonspunkter;
    }
}
