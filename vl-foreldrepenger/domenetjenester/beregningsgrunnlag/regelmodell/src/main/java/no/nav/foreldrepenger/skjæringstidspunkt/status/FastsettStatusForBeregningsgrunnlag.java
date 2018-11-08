package no.nav.foreldrepenger.skjæringstidspunkt.status;

import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStatusForBeregningsgrunnlag.ID)
public class FastsettStatusForBeregningsgrunnlag extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 19.5";
    static final String BESKRIVELSE = "Fastsett status for beregningsgrunnlag";

    FastsettStatusForBeregningsgrunnlag(){
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
        regelmodell.getAktivitetStatuser()
            .forEach(as -> resultater.put("Aktivitetstatus."+as.name(), as.getBeskrivelse()));

        return beregnet(resultater);
    }
}
