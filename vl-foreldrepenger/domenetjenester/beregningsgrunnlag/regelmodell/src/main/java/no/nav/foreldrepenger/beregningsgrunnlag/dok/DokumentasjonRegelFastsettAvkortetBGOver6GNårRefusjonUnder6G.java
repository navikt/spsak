package no.nav.foreldrepenger.beregningsgrunnlag.dok;

import no.nav.foreldrepenger.beregningsgrunnlag.avkorting.RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G;
import no.nav.fpsak.nare.doc.RuleDocumentation;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=254471841")
public class DokumentasjonRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G extends RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G implements BeregningsregelDokumentasjon {

    public DokumentasjonRegelFastsettAvkortetBGOver6GNårRefusjonUnder6G() {
        super(RegelmodellForDokumentasjon.regelmodellMedEttArbeidsforhold);
    }

}
