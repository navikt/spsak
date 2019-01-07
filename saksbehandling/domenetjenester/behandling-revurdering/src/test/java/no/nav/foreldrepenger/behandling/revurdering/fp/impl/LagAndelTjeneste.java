package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;

interface LagAndelTjeneste {

    public void lagAndeler(BeregningsgrunnlagPeriode periode, boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker);

}
