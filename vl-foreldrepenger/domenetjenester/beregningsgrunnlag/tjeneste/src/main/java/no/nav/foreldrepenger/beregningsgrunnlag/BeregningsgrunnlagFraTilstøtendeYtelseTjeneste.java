package no.nav.foreldrepenger.beregningsgrunnlag;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;

public interface BeregningsgrunnlagFraTilstøtendeYtelseTjeneste {

    Beregningsgrunnlag opprettBeregningsgrunnlagFraTilstøtendeYtelse(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag);
}
