package no.nav.foreldrepenger.inngangsvilkaar.adopsjon;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkBarnUnder15År extends LeafSpecification<AdopsjonsvilkårGrunnlag> {

    static final String ID_ES = "FP_VK_4.4";
    static final String ID_FP = "FP_VK_16";

    static final RuleReasonRef INGEN_BARN_UNDER_15 = new RuleReasonRefImpl("1004", "Ingen barn under 15 år ved dato for omsorgsovertakelse.");

    SjekkBarnUnder15År(String id) {
        super(id);
    }

    @Override
    public Evaluation evaluate(AdopsjonsvilkårGrunnlag grunnlag) {
        long antBarn = antallBarnUnder15År(grunnlag);
        if (antBarn > 0) {
            return ja();
        }
        return nei(INGEN_BARN_UNDER_15);
    }

    private long antallBarnUnder15År(AdopsjonsvilkårGrunnlag grunnlag) {
        return grunnlag.getBekreftetAdopsjonBarn()
            .stream()
            .filter(barn -> grunnlag.getOmsorgsovertakelsesdato().minusYears(15).isBefore(barn.getFoedselsdato()))
            .count();
    }

}
