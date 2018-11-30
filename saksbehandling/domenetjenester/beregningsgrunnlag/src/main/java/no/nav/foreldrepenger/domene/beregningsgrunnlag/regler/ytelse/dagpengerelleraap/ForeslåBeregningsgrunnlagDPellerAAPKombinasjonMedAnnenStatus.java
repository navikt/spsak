package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.dagpengerelleraap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Periodeinntekt;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus.ID)
class ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_10.2";
    static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for Dagpenger/AAP i kombinasjon med annen aktivitetstatus";
    private static final BigDecimal TOLV = new BigDecimal("12");

    ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BeregningsgrunnlagPrStatus bgPerStatus = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .filter(bgps -> bgps.getAktivitetStatus().erAAPellerDP())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Ingen aktivitetstatus av type DP eller AAP funnet."));

        AktivitetStatus aktivitetStatus = grunnlag.getBeregningsgrunnlagPrStatus().stream()
            .map(BeregningsgrunnlagPrStatus::getAktivitetStatus)
            .filter(status -> AktivitetStatus.AAP.equals(status) || AktivitetStatus.DP.equals(status))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Ingen aktivitetstatus av type DP eller AAP funnet."));

        Periodeinntekt inntekt = grunnlag.getInntektsgrunnlag().getPeriodeinntekt(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP, grunnlag.getSkjæringstidspunkt())
            .orElseThrow(() -> new IllegalStateException("Ingen inntekter fra tilstøtende ytelser funnet i siste måned med inntekt"));
        BigDecimal utbetalingsgrad = inntekt.getUtbetalingsgrad()
            .orElseThrow(() -> new IllegalStateException("Utbetalingsgrad for DP/AAP mangler."));
        BigDecimal utbetalingsFaktor = utbetalingsgrad.divide(BigDecimal.valueOf(200), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal beregnetPrÅr = inntekt.getInntekt().multiply(TOLV).multiply(utbetalingsFaktor);

        BeregningsgrunnlagPrStatus.builder(bgPerStatus)
            .medBeregnetPrÅr(beregnetPrÅr)
            .medÅrsbeløpFraTilstøtendeYtelse(beregnetPrÅr)
            .build();

        BeregningsgrunnlagHjemmel hjemmel = AktivitetStatus.AAP.equals(bgPerStatus.getAktivitetStatus()) ?
            BeregningsgrunnlagHjemmel.F_14_7 : BeregningsgrunnlagHjemmel.F_14_7_8_49;
        grunnlag.getBeregningsgrunnlag().getAktivitetStatus(bgPerStatus.getAktivitetStatus()).setHjemmel(hjemmel);

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr." + aktivitetStatus.name(), beregnetPrÅr);
        resultater.put("tilstøtendeYtelserPrÅr." + aktivitetStatus.name(), beregnetPrÅr);
        resultater.put("hjemmel", hjemmel);
        return beregnet(resultater);
    }
}
