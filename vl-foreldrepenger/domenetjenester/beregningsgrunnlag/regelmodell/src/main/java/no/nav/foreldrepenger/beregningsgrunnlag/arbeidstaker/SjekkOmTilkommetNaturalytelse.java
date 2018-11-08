package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTilkommetNaturalytelse.ID)
class SjekkOmTilkommetNaturalytelse extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.5";
    static final String BESKRIVELSE = "Har det bruker tilkommet av naturalytelse ved start av perioden?";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkOmTilkommetNaturalytelse(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        LocalDate fom = grunnlag.getSkjæringstidspunkt();
        LocalDate tom = grunnlag.getBeregningsgrunnlagPeriode().getFom();
        Optional<BigDecimal> naturalytelse = grunnlag.getInntektsgrunnlag().finnTotaltNaturalytelseBeløpTilkommetIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), fom, tom);
        return naturalytelse.isPresent() ? ja() : nei();
    }
}
