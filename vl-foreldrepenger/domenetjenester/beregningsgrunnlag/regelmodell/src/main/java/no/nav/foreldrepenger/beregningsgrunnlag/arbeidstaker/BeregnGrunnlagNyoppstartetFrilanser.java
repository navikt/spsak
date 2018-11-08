package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnGrunnlagNyoppstartetFrilanser.ID)
class BeregnGrunnlagNyoppstartetFrilanser extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FB_BR 14.10";
    static final String BESKRIVELSE = "Beregn grunnlag nyoppstartet frilanser = (avklart inntekt * 12)";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    BeregnGrunnlagNyoppstartetFrilanser(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
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
