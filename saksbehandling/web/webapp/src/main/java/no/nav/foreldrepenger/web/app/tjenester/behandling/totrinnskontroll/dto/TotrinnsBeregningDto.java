package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.FaktaOmBeregningTilfelle;

public class TotrinnsBeregningDto {

    private boolean fastsattVarigEndringNaering;

    private List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller;

    public void setFastsattVarigEndringNaering(boolean fastsattVarigEndringNaering) {
        this.fastsattVarigEndringNaering = fastsattVarigEndringNaering;
    }

    public boolean isFastsattVarigEndringNaering() {
        return fastsattVarigEndringNaering;
    }

    public List<FaktaOmBeregningTilfelle> getFaktaOmBeregningTilfeller() {
        return faktaOmBeregningTilfeller;
    }

    public void setFaktaOmBeregningTilfeller(List<FaktaOmBeregningTilfelle> faktaOmBeregningTilfeller) {
        this.faktaOmBeregningTilfeller = faktaOmBeregningTilfeller;
    }
}
