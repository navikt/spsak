package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.vedtak.util.BevegeligeHelligdagerUtil;
import no.nav.vedtak.util.FPDateUtil;

@RuleDocumentation(FastsettSammenligningsgrunnlag.ID)
class FastsettSammenligningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 17.1";
    static final String BESKRIVELSE = "Sammenligningsgrunnlag er sum av inntektene i sammenligningsperioden";

    FastsettSammenligningsgrunnlag() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        if (grunnlag.getBeregningsgrunnlag().getSammenligningsGrunnlag() == null) {
            Periode sammenligningsPeriode = lagSammenligningsPeriode(grunnlag.getInntektsgrunnlag(), grunnlag.getSkjæringstidspunkt());
            BigDecimal sammenligningsgrunnlagInntekt = grunnlag.getInntektsgrunnlag().getSamletInntektISammenligningsperiode(sammenligningsPeriode);
            SammenligningsGrunnlag sg = SammenligningsGrunnlag.builder()
                .medSammenligningsperiode(sammenligningsPeriode)
                .medRapportertPrÅr(sammenligningsgrunnlagInntekt)
                .build();
            Beregningsgrunnlag.builder(grunnlag.getBeregningsgrunnlag()).medSammenligningsgrunnlag(sg).build();
        }

        Map<String, Object> resultater = new HashMap<>();
        SammenligningsGrunnlag sammenligningsGrunnlag = grunnlag.getBeregningsgrunnlag().getSammenligningsGrunnlag();
        resultater.put("sammenligningsperiode", sammenligningsGrunnlag.getSammenligningsperiode());
        resultater.put("sammenligningsgrunnlagPrÅr", sammenligningsGrunnlag.getRapportertPrÅr());
        return beregnet(resultater);
    }

    private Periode lagSammenligningsPeriode(Inntektsgrunnlag inntektsgrunnlag, LocalDate skjæringstidspunkt) {
        LocalDate behandlingsTidspunkt = LocalDate.now(FPDateUtil.getOffset());
        LocalDate gjeldendeTidspunkt = behandlingsTidspunkt.isBefore(skjæringstidspunkt) ? behandlingsTidspunkt : skjæringstidspunkt;

        Optional<LocalDate> sisteRapporterteInntektMåned = inntektsgrunnlag.sistePeriodeMedInntektFørDato(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, gjeldendeTidspunkt);
        LocalDate sisteØnskedeInntektMåned = gjeldendeTidspunkt.minusMonths(1).withDayOfMonth(1);
        if (sisteRapporterteInntektMåned.filter(inntekt -> inntekt.withDayOfMonth(1).equals(sisteØnskedeInntektMåned)).isPresent()) {
            return lag12MånedersPeriode(sisteRapporterteInntektMåned.get());
        }
        if (erEtterRapporteringsFrist(inntektsgrunnlag.getInntektRapporteringFristDag(), gjeldendeTidspunkt, behandlingsTidspunkt)) {
            return lag12MånedersPeriode(sisteØnskedeInntektMåned);
        }
        return lag12MånedersPeriode(sisteØnskedeInntektMåned.minusMonths(1));
    }

    private static boolean erEtterRapporteringsFrist(int inntektRapporteringFristDag, LocalDate gjeldendeTidspunkt, LocalDate nåtid) {
        LocalDate fristUtenHelligdager = gjeldendeTidspunkt.withDayOfMonth(1).minusDays(1).plusDays(inntektRapporteringFristDag);
        LocalDate fristMedHelligdager = BevegeligeHelligdagerUtil.hentFørsteVirkedagFraOgMed(fristUtenHelligdager);
        return nåtid.isAfter(fristMedHelligdager);
    }

    private static Periode lag12MånedersPeriode(LocalDate periodeTom) {
        LocalDate tom = periodeTom.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate fom = tom.minusYears(1).plusMonths(1).withDayOfMonth(1);
        return Periode.of(fom, tom);
    }
}
