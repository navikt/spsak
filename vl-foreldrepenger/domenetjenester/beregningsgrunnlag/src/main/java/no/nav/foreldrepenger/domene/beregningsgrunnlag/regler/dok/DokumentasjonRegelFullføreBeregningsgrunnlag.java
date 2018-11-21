package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.RegelFullføreBeregningsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;

/**
 * Det mangler dokumentasjon
 */

@SuppressWarnings("unchecked")
@RuleDocumentation(value = RegelFullføreBeregningsgrunnlag.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class DokumentasjonRegelFullføreBeregningsgrunnlag extends RegelFullføreBeregningsgrunnlag implements BeregningsregelDokumentasjon {

    public DokumentasjonRegelFullføreBeregningsgrunnlag() {
        super(RegelmodellForDokumentasjon.regelmodellMedEttArbeidsforhold);
    }

}
