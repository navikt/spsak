package no.nav.foreldrepenger.regler.søknadsfrist.betingelser;

import no.nav.foreldrepenger.regler.søknadsfrist.grunnlag.SøknadsfristGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmDetSøkesOmUttak.ID)
public class SjekkOmDetSøkesOmUttak extends LeafSpecification<SøknadsfristGrunnlag> {

    public static final String ID = "FP_VK XX8";

    public SjekkOmDetSøkesOmUttak() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(SøknadsfristGrunnlag søknadsfristGrunnlag) {
        if (søknadsfristGrunnlag.isErSøknadOmUttak()) {
            return ja();
        }
        return nei();
    }
}
