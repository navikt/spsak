package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OpprettAndelerForNyeArbeidsforhold.ID)
class OpprettAndelerForNyeArbeidsforhold extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.4";
    static final String BESKRIVELSE = "Opprette eventuelle andeler for nye arbeidsforhold";

    OpprettAndelerForNyeArbeidsforhold() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        Map<String, Object> resultater = new LinkedHashMap<>();
        beregningsgrunnlag.getBeregningsgrunnlagAndeler().stream().filter(andel -> !andel.erFraTilstøtendeYtelse()).forEach(andel -> {
            String andelNavn = "andel[arbeidsgiverId="+andel.getIdentifikator()+"].";
            resultater.put(andelNavn + "AktivitetStatus", andel.getAktivitetStatus());
            resultater.put(andelNavn + "ArbeidsforholdId", andel.getArbeidsforholdId());
            resultater.put(andelNavn + "Fom", andel.getArbeidsperiodeFom());
            resultater.put(andelNavn + "Tom", andel.getArbeidsperiodeTom());
            resultater.put(andelNavn + "Inntektskategori", andel.getInntektskategori().name());
            resultater.put(andelNavn + "Refusjonskrav", andel.getRefusjonskrav().orElse(null));
        });

        return beregnet(resultater);
    }
}
