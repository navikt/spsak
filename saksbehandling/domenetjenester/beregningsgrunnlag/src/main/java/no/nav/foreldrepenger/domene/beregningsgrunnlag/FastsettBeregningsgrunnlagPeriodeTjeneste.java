package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper.IdentifisertePeriodeÅrsaker;

public interface FastsettBeregningsgrunnlagPeriodeTjeneste {

    void fastsettPerioder(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag);

    IdentifisertePeriodeÅrsaker identifiserPeriodeÅrsaker(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag);
}
