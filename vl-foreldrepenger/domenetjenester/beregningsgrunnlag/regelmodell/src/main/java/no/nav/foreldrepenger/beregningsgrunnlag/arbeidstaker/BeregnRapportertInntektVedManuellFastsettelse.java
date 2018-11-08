package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnRapportertInntektVedManuellFastsettelse.ID)
class BeregnRapportertInntektVedManuellFastsettelse extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FB_BR 15.6";
    static final String BESKRIVELSE = "Rapportert inntekt = manuelt fastsatt månedsinntekt * 12";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnRapportertInntektVedManuellFastsettelse(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        Objects.requireNonNull(arbeidsforhold, "arbeidsforhold");
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        resultater.put("beregnetPrÅr", arbeidsforhold.getBeregnetPrÅr());
        return beregnet(resultater);
    }
}
