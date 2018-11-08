package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkFødselErRegistrert extends LeafSpecification<FødselsvilkårGrunnlag> {

    static final String ID = SjekkFødselErRegistrert.class.getSimpleName();

    SjekkFødselErRegistrert() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FødselsvilkårGrunnlag t) {
        boolean fødselRegistrert = t.getBekreftetFoedselsdato() != null && t.getAntallBarn() > 0;
        if (fødselRegistrert) {
            return ja();
        } else {
            return nei();
        }
    }

}
