package no.nav.foreldrepenger.beregningsgrunnlag.dok;

import no.nav.foreldrepenger.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;

/**
 * Det mangler dokumentasjon
 */

@SuppressWarnings("unchecked")
@RuleDocumentation(value = RegelForeslåBeregningsgrunnlag.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class DokumentasjonRegelForeslåBeregningsgrunnlag extends RegelForeslåBeregningsgrunnlag implements BeregningsregelDokumentasjon {

    public DokumentasjonRegelForeslåBeregningsgrunnlag() {
        super(RegelmodellForDokumentasjon.regelmodellMedEttArbeidsforhold);
    }

}
