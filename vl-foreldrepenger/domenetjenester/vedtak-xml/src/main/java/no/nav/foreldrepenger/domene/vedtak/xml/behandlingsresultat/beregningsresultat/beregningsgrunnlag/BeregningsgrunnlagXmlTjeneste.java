package no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;

public interface BeregningsgrunnlagXmlTjeneste {
    void setBeregningsgrunnlag(Beregningsresultat beregningsresultat, Behandling behandling);
}
