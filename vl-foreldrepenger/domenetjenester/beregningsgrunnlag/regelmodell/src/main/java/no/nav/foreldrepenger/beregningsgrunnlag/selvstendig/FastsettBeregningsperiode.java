package no.nav.foreldrepenger.beregningsgrunnlag.selvstendig;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettBeregningsperiode.ID)
public class FastsettBeregningsperiode extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final BeregningsgrunnlagHjemmel HJEMMEL = BeregningsgrunnlagHjemmel.HJEMMEL_BARE_SELVSTENDIG;
    static final String ID = "FP_BR 2.1 BP";
    private static final String BESKRIVELSE = "Fastsett beregningsperiode";

    public FastsettBeregningsperiode() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        Map<String, Object> resultater = new HashMap<>();
        if (grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL) == null) {
            grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.SN).setHjemmel(HJEMMEL);
            resultater.put("hjemmel", HJEMMEL);
        }
        Optional<LocalDate> sisteLigningsdatoOpt = grunnlag.getInntektsgrunnlag().sistePeriodeMedInntektFørDato(Inntektskilde.SIGRUN, grunnlag.getSkjæringstidspunkt());
        LocalDate tidligstMuligBeregningsår = grunnlag.getSkjæringstidspunkt().minusYears(4);
        LocalDate tom;
        LocalDate fom;
        if (sisteLigningsdatoOpt.isPresent()) {
            LocalDate sisteLigningsdato = sisteLigningsdatoOpt.get();
            if (sisteLigningsdato.getYear() <= tidligstMuligBeregningsår.plusYears(2).getYear()) {
                fom = tidligstMuligBeregningsår.withMonth(1).withDayOfMonth(1);
                tom = fom.plusYears(2).withMonth(12).withDayOfMonth(31);
            } else {
                tom = sisteLigningsdato.withMonth(12).withDayOfMonth(31);
                fom = tom.minusYears(2).withMonth(1).withDayOfMonth(1);
            }
        } else {
            fom = tidligstMuligBeregningsår.withMonth(1).withDayOfMonth(1);
            tom = fom.plusYears(2).withMonth(12).withDayOfMonth(31);
        }
        BeregningsgrunnlagPrStatus.builder(bgps).medBeregningsperiode(Periode.of(fom, tom)).build();
        return beregnet(resultater);
    }
}
