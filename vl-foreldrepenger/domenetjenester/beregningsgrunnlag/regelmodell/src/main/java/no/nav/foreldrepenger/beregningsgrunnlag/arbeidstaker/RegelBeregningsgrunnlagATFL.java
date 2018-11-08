package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import java.util.List;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregningsgrunnlagATFL implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_14-15-27-28";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelBeregningsgrunnlagATFL(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        IkkeBeregnet fastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold = new FastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold();

        IkkeBeregnet fastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold = new FastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold();


        // FP_BR 27.4 Er perioden opprettet som følge av et tidsbegrenset arbeidsforhold?

        Specification<BeregningsgrunnlagPeriode> sjekkOmPeriodenErEtterTidsbegrensetArbeidsforhold =
            rs.beregningHvisRegel(new SjekkPeriodeÅrsakErTidsbegrensetArbeidsforhold(), fastsettesVedSkjønnEtterTidsbegrensetArbeidsforhold, fastsettesVedSkjønnUtenTidsbegrensetArbeidsforhold);


        // FP_BR 27.1 Har rapportert inntekt inkludert bortfaltnaturalytelse for 1. periode avvik mot sammenligningsgrunnlag > 25%?

        Specification<BeregningsgrunnlagPeriode> sjekkÅrsinntektMotSammenligningsgrunnlag =
                rs.beregningHvisRegel(new SjekkÅrsinntektMotSammenligningsgrunnlag(), sjekkOmPeriodenErEtterTidsbegrensetArbeidsforhold, new Beregnet());

        // FP_BR 17.1 17.2 27.1 Sammenligningsgrunnlag pr år = sum av 12 siste måneder

        Specification<BeregningsgrunnlagPeriode> fastsettSammenligningsgrunnlag =
                rs.beregningsRegel("FP_BR 17.1", "Fastsett sammenligningsgrunnlag for ATFL",
                        new FastsettSammenligningsgrunnlag(), sjekkÅrsinntektMotSammenligningsgrunnlag);

        // Første beregningsgrunnlagsperiode? Sammenligninggrunnlag skal fastsettes og sjekkes mot bare om det er første periode

        Specification<BeregningsgrunnlagPeriode> sjekkOmFørstePeriode =
            rs.beregningHvisRegel(new SjekkOmFørsteBeregningsgrunnlagsperiode(), fastsettSammenligningsgrunnlag, new Beregnet());


        // Har bruker kombinasjonsstatus?

        Specification<BeregningsgrunnlagPeriode> sjekkHarBrukerKombinasjonsstatus =
                rs.beregningHvisRegel(new SjekkHarBrukerKombinasjonsstatus(), new Beregnet(), sjekkOmFørstePeriode);

        // FP_BR 14.3 14.5 14.6 28.4 Beregnet pr år = sum alle inntekter

        Specification<BeregningsgrunnlagPeriode> fastsettBeregnetPrÅr =
                rs.beregningsRegel("FP_BR 14.5", "Fastsett beregnet pr år for ATFL",
                        new FastsettBeregnetPrÅr(), sjekkHarBrukerKombinasjonsstatus);

        // For hver arbeidsgiver eller frilansinntekt: Fastsett brutto pr periodetype
        // FB_BR 14.3 28.2 Brutto pr periodetype = snitt av fastsatte inntekter av A-ordning * 12
        // FP_BR 15.2 Brutto pr periode_type = inntektsmelding sats * 12
        // FP_BR 15.1 Foreligger inntektsmelding?

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagATFL =
                rs.beregningsRegel("FP_BR 14.X", "Fastsett beregningsgrunnlag pr arbeidsforhold",
                    RegelBeregnBruttoPrArbeidsforhold.class, regelmodell, "arbeidsforhold", arbeidsforhold, fastsettBeregnetPrÅr);

        // FP_BR X.X Ingen regelberegning hvis besteberegning gjelder

        Specification<BeregningsgrunnlagPeriode> sjekkOmBesteberegning =
                rs.beregningHvisRegel(new SjekkOmBesteberegning(), new Beregnet(), beregningsgrunnlagATFL);

        return sjekkOmBesteberegning;
    }
}
