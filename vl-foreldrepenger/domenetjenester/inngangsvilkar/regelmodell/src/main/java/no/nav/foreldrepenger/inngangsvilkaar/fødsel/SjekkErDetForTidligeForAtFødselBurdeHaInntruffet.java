package no.nav.foreldrepenger.inngangsvilkaar.fødsel;

import java.time.LocalDate;

import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.vedtak.util.FPDateUtil;

public class SjekkErDetForTidligeForAtFødselBurdeHaInntruffet extends LeafSpecification<FødselsvilkårGrunnlag> {

    static final String ID = SjekkErDetForTidligeForAtFødselBurdeHaInntruffet.class.getSimpleName();

    static final RuleReasonRef FØDSEL_BURDE_HA_INNTRUFFET = new RuleReasonRefImpl("1026", "Fødsel ikke funnet i folkeregisteret");

    private static final int MAX_ANTALL_DAGER_ETTER_TERMIN = 25;

    public SjekkErDetForTidligeForAtFødselBurdeHaInntruffet() {
        super(ID);
    }

    @Override
    public Evaluation evaluate(FødselsvilkårGrunnlag grunnlag) {
        if (!grunnlag.isErSøktOmTermin()){
            return nei(FØDSEL_BURDE_HA_INNTRUFFET);
        }
        LocalDate nå = LocalDate.now(FPDateUtil.getOffset());
        LocalDate nårFødselBurdeHaInntruffet = grunnlag.getBekreftetTermindato().plusDays(MAX_ANTALL_DAGER_ETTER_TERMIN);
        if (nå.isAfter(nårFødselBurdeHaInntruffet)) {
            return nei(FØDSEL_BURDE_HA_INNTRUFFET);
        }
        return ja();
    }

}
