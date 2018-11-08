package no.nav.foreldrepenger.inngangsvilkaar.søknadsfrist;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.SoeknadsfristvilkarGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(value="FP_VK_3.1")
public class SjekkElektroniskSøknad extends LeafSpecification<SoeknadsfristvilkarGrunnlag> {
    static final String ID = SjekkElektroniskSøknad.class.getSimpleName();

    SjekkElektroniskSøknad() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(SoeknadsfristvilkarGrunnlag t) {
        boolean erElektroniskSøknad = t.isElektroniskSoeknad();  // ellers papir
        if(erElektroniskSøknad) {
            return ja();
        } else {
            return nei();
        }
    }

}
