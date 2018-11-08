package no.nav.foreldrepenger.beregningsgrunnlag.fastsette.refusjon.over6g;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnArbeidsgiversAndeler.ID)
class BeregnArbeidsgiversAndeler extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final String PROSENTANDEL = "prosentandel.";
    private static final String AVKORTET_REFUSJON_PR_ÅR = "avkortetRefusjonPrÅr.";
    private static final String REFUSJON_TIL_FORDELING = "refusjonTilFordeling";
    private static final Double NULL = 0.0d;
    static final String ID = "FP_BR_29.13.1";
    static final String BESKRIVELSE = "Beregn hver arbeidsgivers andel av det som skal gjenstår å refundere";

    BeregnArbeidsgiversAndeler() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        SingleEvaluation resultat = ja();
        final Map<String, Object> resultater = new HashMap<>();
        resultat.setEvaluationProperties(resultater);
        // Finn alle arbeidsforhold som ikke er avkortet allerede
        List<BeregningsgrunnlagPrArbeidsforhold> ikkeFastsatt = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream()
            .filter(af -> af.getMaksimalRefusjonPrÅr() != null)
            .filter(af -> af.getAvkortetPrÅr() == null)
            .collect(Collectors.toList());
        if (ikkeFastsatt.isEmpty()) {
            resultater.put(REFUSJON_TIL_FORDELING, NULL);
            return resultat;
        }
        // Gjenstående refusjon til fordeling er 6G minus det som allerede er tildelt tidligere
        BigDecimal seksG = grunnlag.getRedusertGrunnbeløp().multiply(BigDecimal.valueOf(6));
        resultater.put("redusertGrunnbeløp", grunnlag.getRedusertGrunnbeløp());
        BigDecimal refusjonTilFordeling = seksG.subtract(
                grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream()
                    .filter(af -> af.getAvkortetRefusjonPrÅr() != null)
                    .map(BeregningsgrunnlagPrArbeidsforhold::getAvkortetRefusjonPrÅr)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO));

        // Forsøk å tildele avkortede andeler til alle
        BigDecimal bruttoSum = ikkeFastsatt.stream().map(BeregningsgrunnlagPrArbeidsforhold::getBruttoPrÅr).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        resultater.put(REFUSJON_TIL_FORDELING, refusjonTilFordeling);
        ikkeFastsatt.forEach(af -> {
            BigDecimal andel = refusjonTilFordeling.multiply(af.getBruttoPrÅr()).divide(bruttoSum, 10, RoundingMode.HALF_EVEN);
            BeregningsgrunnlagPrArbeidsforhold.builder(af)
                .medAvkortetRefusjonPrÅr(andel)
                .build();
            resultater.put(AVKORTET_REFUSJON_PR_ÅR + af.getArbeidsgiverId(), af.getAvkortetRefusjonPrÅr());
            resultater.put(PROSENTANDEL + af.getArbeidsgiverId(), BigDecimal.valueOf(100).multiply(af.getBruttoPrÅr()).divide(bruttoSum, 10, RoundingMode.HALF_EVEN));
        });

        // Hvis noen er tildelt mer enn kravet - reduser disse til kravet og sett andre til null
        Optional<BeregningsgrunnlagPrArbeidsforhold> tildeltMerEnnKravet = ikkeFastsatt.stream()
                .filter(af -> af.getAvkortetRefusjonPrÅr().compareTo(af.getMaksimalRefusjonPrÅr()) > 0)
                .findAny();
        if (tildeltMerEnnKravet.isPresent()) {
            final Map<String, Object> korrigerteResultater = new HashMap<>();
            korrigerteResultater.put(REFUSJON_TIL_FORDELING, refusjonTilFordeling);
            resultat.setEvaluationProperties(korrigerteResultater);
            ikkeFastsatt.forEach(af -> {
                BigDecimal avkortet = (af.getAvkortetRefusjonPrÅr().compareTo(af.getMaksimalRefusjonPrÅr()) > 0 ? af.getMaksimalRefusjonPrÅr() : null);
                BeregningsgrunnlagPrArbeidsforhold.builder(af)
                    .medAvkortetRefusjonPrÅr(avkortet)
                    .build();
                korrigerteResultater.put(AVKORTET_REFUSJON_PR_ÅR + af.getArbeidsgiverId(), af.getAvkortetRefusjonPrÅr());
            });
        }
        return resultat;
    }
}
