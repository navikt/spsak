package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagFraTilstøtendeYtelse;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskategori;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.RelatertYtelseType;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.TilstøtendeYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTYerSykepengerMedKriterier.ID)
class SjekkOmTYerSykepengerMedKriterier extends LeafSpecification<BeregningsgrunnlagFraTilstøtendeYtelse> {

    static final String ID = "FP_BR 25.9";
    static final String BESKRIVELSE = "Er den tilstøtende ytelsen sykepenger med reduksjon 65% og bruker har Arbeidskategori 07 Inaktiv?";

    SjekkOmTYerSykepengerMedKriterier() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagFraTilstøtendeYtelse beregningsgrunnlag) {
        TilstøtendeYtelse tilstøtendeYtelse = beregningsgrunnlag.getTilstøtendeYtelse();
        boolean erSykepenger = RelatertYtelseType.SYKEPENGER.equals(tilstøtendeYtelse.getRelatertYtelseType());
        boolean reduksjon65prosent = Dekningsgrad.DEKNINGSGRAD_65.equals(tilstøtendeYtelse.getDekningsgrad());
        boolean arbeidskategoriInaktiv = tilstøtendeYtelse.getInntektskategoriListe().contains(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);

        SingleEvaluation resultat = erSykepenger && reduksjon65prosent && arbeidskategoriInaktiv
            ? ja() : nei();
        resultat.setEvaluationProperty("erSykepenger", erSykepenger);
        resultat.setEvaluationProperty("reduksjon65prosent", reduksjon65prosent);
        resultat.setEvaluationProperty("arbeidskategoriInaktiv", arbeidskategoriInaktiv);
        return resultat;
    }
}
