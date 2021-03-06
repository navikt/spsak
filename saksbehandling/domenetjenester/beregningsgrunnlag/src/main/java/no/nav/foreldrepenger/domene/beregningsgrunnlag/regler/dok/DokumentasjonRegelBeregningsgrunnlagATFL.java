package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.dok;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.fpsak.nare.doc.RuleDocumentation;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelBeregningsgrunnlagATFL.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class DokumentasjonRegelBeregningsgrunnlagATFL extends RegelBeregningsgrunnlagATFL implements BeregningsregelDokumentasjon {

    public DokumentasjonRegelBeregningsgrunnlagATFL() {
        super(RegelmodellForDokumentasjon.regelmodellMedEttArbeidsforhold);
    }

}
