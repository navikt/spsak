package no.nav.foreldrepenger.beregningsgrunnlag.avkorting;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.ID)
class AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.4";
    static final String BESKRIVELSE = "Avkort alle beregningsgrunnlagsandeler som ikke gjelder arbeidsforhold andelsmessig.";

    AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        BeregningsgrunnlagPrStatus atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BigDecimal sumBeregningsgrunnlagArbeidsforhold = atfl == null ? BigDecimal.ZERO : atfl.getArbeidsforholdIkkeFrilans()
            .stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal seksG = grunnlag.getRedusertGrunnbeløp().multiply(BigDecimal.valueOf(6));
        resultater.put("redusertGrunnbeløp", grunnlag.getRedusertGrunnbeløp());
        BigDecimal bGUtenArbeidsforholdTilFordeling = seksG.subtract(sumBeregningsgrunnlagArbeidsforhold);

        // inntekt knyttet til frilans må fordeles først
        if (atfl != null ) {
            Optional<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdOptional = atfl.getFrilansArbeidsforhold();
            if(arbeidsforholdOptional.isPresent()) {
                BeregningsgrunnlagPrArbeidsforhold af = arbeidsforholdOptional.get();
                BigDecimal bruttoBeregningsgrunnlagForAndelen = af.getBruttoInkludertNaturalytelsePrÅr();
                BigDecimal avkortetBrukersAndel;
                if (bruttoBeregningsgrunnlagForAndelen.compareTo(bGUtenArbeidsforholdTilFordeling) >= 0) {
                    avkortetBrukersAndel = bGUtenArbeidsforholdTilFordeling;
                    lagResultaterFrilanser(af, avkortetBrukersAndel, resultater);
                    grunnlag.getBeregningsgrunnlagPrStatus().stream()
                        .filter(bgps -> !bgps.erArbeidstakerEllerFrilanser())
                        .forEach(bgp -> lagResultaterUtenArbeidsforhold(bgp, BigDecimal.ZERO, resultater));
                    return beregnet(resultater);
                } else {
                    avkortetBrukersAndel = bruttoBeregningsgrunnlagForAndelen;
                    bGUtenArbeidsforholdTilFordeling = bGUtenArbeidsforholdTilFordeling.subtract(avkortetBrukersAndel);
                    lagResultaterFrilanser(af, avkortetBrukersAndel, resultater);
                }
            }
        }

        // sortere etter avkorting prioritet for beregningsgrunnlag uten arbeidsforhold
        List<BeregningsgrunnlagPrStatus> bgpsSorted = finnAlleBGUtenArbeidsForholdSorterte(grunnlag);
        Iterator<BeregningsgrunnlagPrStatus> bgpsIter = bgpsSorted.iterator();
        while (bgpsIter.hasNext()) {
            BeregningsgrunnlagPrStatus bgps = bgpsIter.next();
            BigDecimal bruttoBeregningsgrunnlagForAndelen = bgps.getBruttoInkludertNaturalytelsePrÅr();
            BigDecimal avkortetBrukersAndel;
            if (bruttoBeregningsgrunnlagForAndelen.compareTo(bGUtenArbeidsforholdTilFordeling) >= 0) {
                avkortetBrukersAndel = bGUtenArbeidsforholdTilFordeling;
                lagResultaterUtenArbeidsforhold(bgps, avkortetBrukersAndel, resultater);
                bgpsIter.forEachRemaining(bgp -> lagResultaterUtenArbeidsforhold(bgp, BigDecimal.ZERO, resultater));
                return beregnet(resultater);
            } else {
                avkortetBrukersAndel = bruttoBeregningsgrunnlagForAndelen;
                bGUtenArbeidsforholdTilFordeling = bGUtenArbeidsforholdTilFordeling.subtract(avkortetBrukersAndel);
                lagResultaterUtenArbeidsforhold(bgps, avkortetBrukersAndel, resultater);
            }
        }
        return beregnet(resultater);
    }

    private List<BeregningsgrunnlagPrStatus> finnAlleBGUtenArbeidsForholdSorterte(BeregningsgrunnlagPeriode grunnlag){
        return grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .filter(bgps -> !bgps.erArbeidstakerEllerFrilanser())
            .sorted(Comparator.comparingInt(bgps -> bgps.getAktivitetStatus().getAvkortingPrioritet()))
            .collect(Collectors.toList());
    }

    private void lagResultaterFrilanser(BeregningsgrunnlagPrArbeidsforhold af, BigDecimal avkortetBrukersAndel, Map<String, Object> resultater) {
        BeregningsgrunnlagPrArbeidsforhold.builder(af).medAvkortetRefusjonPrÅr(BigDecimal.ZERO).medAvkortetBrukersAndelPrÅr(avkortetBrukersAndel).medAvkortetPrÅr(avkortetBrukersAndel).build();
        resultater.put("avkortetPrÅr.arbeidsforhold." + af.getArbeidsgiverId(), af.getAvkortetBrukersAndelPrÅr());
    }

    private void lagResultaterUtenArbeidsforhold(BeregningsgrunnlagPrStatus bgps, BigDecimal avkortetBrukersAndel, Map<String, Object> resultater) {
        BeregningsgrunnlagPrStatus.builder(bgps).medAvkortetPrÅr(avkortetBrukersAndel).build();
        resultater.put("avkortetPrÅr.status." + bgps.getAktivitetStatus().name(), bgps.getAvkortetPrÅr());
    }
}
