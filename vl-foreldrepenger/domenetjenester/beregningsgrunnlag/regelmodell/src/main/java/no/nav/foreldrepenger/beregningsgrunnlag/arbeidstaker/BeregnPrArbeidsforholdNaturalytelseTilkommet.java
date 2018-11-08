package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdNaturalytelseTilkommet.ID)
class BeregnPrArbeidsforholdNaturalytelseTilkommet extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.6";
    static final String BESKRIVELSE = "Beregn naturalytelse -> naturalytelseverdi * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnPrArbeidsforholdNaturalytelseTilkommet(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        LocalDate fom = grunnlag.getSkjæringstidspunkt();
        LocalDate tom = grunnlag.getBeregningsgrunnlagPeriode().getFom();
        Inntektsgrunnlag inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        BigDecimal beløp = inntektsgrunnlag.finnTotaltNaturalytelseBeløpTilkommetIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), fom, tom)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ikke være mulig å havne i denne regelen uten beløp."));
        BigDecimal naturalytelseTilkommetPrÅr = beløp.multiply(BigDecimal.valueOf(12));

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medNaturalytelseTilkommetPrÅr(naturalytelseTilkommetPrÅr)
            .build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("naturalytelseTilkommetPrÅr", naturalytelseTilkommetPrÅr);
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }
}
