package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import java.util.Objects;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.Kjoenn;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkSøkerErKvinne extends LeafSpecification<FødselsvilkårGrunnlag> {

    static final String ID = SjekkSøkerErKvinne.class.getSimpleName();

    static final RuleReasonRefImpl IKKE_OPPFYLT_SØKER_ER_KVINNE = new RuleReasonRefImpl("1003", "Søker er ikke KVINNE({0}), er {1}");

    SjekkSøkerErKvinne() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FødselsvilkårGrunnlag t) {
        boolean erKvinne = Objects.equals(Kjoenn.KVINNE, t.getSoekersKjonn());
        if (erKvinne) {
            return ja();
        } else {
            return nei(IKKE_OPPFYLT_SØKER_ER_KVINNE, Kjoenn.KVINNE, t.getSoekersKjonn());
        }
    }

}
