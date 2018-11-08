package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import java.util.Objects;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.SoekerRolle;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class SjekkSøkerErMor extends LeafSpecification<FødselsvilkårGrunnlag> {

    static final String ID = SjekkSøkerErMor.class.getSimpleName();

    static final RuleReasonRefImpl IKKE_OPPFYLT_FØDSEL_REGISTRERT_SØKER_IKKE_BARNETS_MOR = new RuleReasonRefImpl("1002",
            "Søker er ikke barnets mor");

    SjekkSøkerErMor() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FødselsvilkårGrunnlag t) {
        boolean erBarnetsMor = Objects.equals(t.getSoekerRolle(), SoekerRolle.MORA);
        if (erBarnetsMor) {
            return ja();
        } else {
            return nei();
        }
    }

    @Override
    public String beskrivelse() {
        return "Sjekk søker er mor.";
    }
}
