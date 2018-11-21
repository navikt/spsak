package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagAndelTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(OppjusterInntektTilÅrbeløp.ID)
class OppjusterInntektTilÅrbeløp extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.6";
    static final String BESKRIVELSE = "Oppjustere beregningsgrunnlagsandelene iht tidskode";

    OppjusterInntektTilÅrbeløp() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        Map<String, Object> resultater = new HashMap<>();
        beregningsgrunnlag.getBeregningsgrunnlagAndeler().stream()
            .filter(BeregningsgrunnlagAndelTilstøtendeYtelse::erFraTilstøtendeYtelse)
            .forEach(andel -> {
                BigDecimal beregnetPrÅr = andel.getBeløp().multiply(andel.getHyppighet().getAntallPrÅr());
                BeregningsgrunnlagAndelTilstøtendeYtelse.builder(andel).medBeregnetPrÅr(beregnetPrÅr).build();
                String identifikator = AktivitetStatus.ATFL.equals(andel.getAktivitetStatus()) ? andel.getIdentifikator() : andel.getAktivitetStatus().name();
                resultater.put("andel[arbeidsgiverId=" + identifikator + "].beregnetPrÅr", beregnetPrÅr);
            });
        return beregnet(resultater);
    }
}
