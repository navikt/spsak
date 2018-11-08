package no.nav.foreldrepenger.skjæringstidspunkt.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettKombinasjoner.ID)
public class FastsettKombinasjoner extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR_19_4";
    static final String BESKRIVELSE = "Sett kombinasjoner";

    FastsettKombinasjoner() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        List<AktivitetStatus> aktivitetStatuser = regelmodell.getAktivitetStatuser();
        List<AktivitetStatus> kombinasjonStatus = Arrays.asList(AktivitetStatus.ATFL, AktivitetStatus.SN);
        Map<String, Object> resultater = new HashMap<>();
        if(aktivitetStatuser.containsAll(kombinasjonStatus)){
            aktivitetStatuser.add(AktivitetStatus.ATFL_SN);
            aktivitetStatuser.removeAll(kombinasjonStatus);
            resultater.put("aktivitetStatus", AktivitetStatus.ATFL_SN.getBeskrivelse());
        }
        return beregnet(resultater);
    }
}
