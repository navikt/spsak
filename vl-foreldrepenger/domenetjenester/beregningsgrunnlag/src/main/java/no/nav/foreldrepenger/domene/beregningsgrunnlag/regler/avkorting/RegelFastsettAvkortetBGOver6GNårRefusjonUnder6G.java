package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.avkorting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G implements RuleService<BeregningsgrunnlagPeriode> {
    public static final String ID = "FP_BR_29.8";
    public static final String BESKRIVELSE = "Fastsett avkortet BG over 6G når refusjon under 6G";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        BeregningsgrunnlagPrStatus bgpsa = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);

        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        //FP_BR_29.8.10 For alle beregningsgrunnlagsandeler som gjelder arbeidsforhold, fastsett Brukers Andel
        //FP_BR_29.8.4 Avkort alle beregningsgrunnlagsander som ikke gjelder arbeidsforhold andelsmessig
        Specification<BeregningsgrunnlagPeriode> avkortAndelerAndelsmessigOgFastsettBrukersAndel = rs.beregningsRegel(AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.ID,
                AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig.BESKRIVELSE,
                new AvkortBGAndelerSomIkkeGjelderArbeidsforholdAndelsmessig(), new FastsettBrukersAndelForBGAndelerSomGjelderArbeidsforhold());

        Specification<BeregningsgrunnlagPeriode> avkortAndelerSomIkkegjelderAFtil0 = new Beregnet();

        if (bgpsa != null) {

        //FP_BR_29.8.6-9 Fastsett andel til fordeling - Itereres over like mange ganger som antall arbeidsforhold.
            List<Specification<BeregningsgrunnlagPeriode>> prArbeidsforhold = new ArrayList<>();
            bgpsa.getArbeidsforhold().forEach(af -> prArbeidsforhold.add(opprettRegelFastsettUtbetalingsbeløpTilBruker()));
            Specification<BeregningsgrunnlagPeriode> fastsettUtbetalingsbeløpTilBrukerChain = rs.beregningsRegel(ID, BESKRIVELSE, prArbeidsforhold, new Beregnet());

            //FP_BR_29.8.3 Avkort alle beregningsgrunnlagsandeler som ikke gjelder arbeidsforhold til 0
            avkortAndelerSomIkkegjelderAFtil0 = rs.beregningsRegel(AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0.ID, AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0.BESKRIVELSE,
                new AvkortBGAndelerSomIkkeGjelderArbeidsforholdTil0(), fastsettUtbetalingsbeløpTilBrukerChain);

        }

        //FP_BR_29.8.2 Er totalt BG for beregningsgrunnlagsandeler fra arbeidsforhold > 6G?
        Specification<BeregningsgrunnlagPeriode> erTotaltBGFraArbeidforholdStørreEnn6G = rs.beregningHvisRegel(new SjekkOmTotaltBGForArbeidsforholdStørreEnnAntallG(BigDecimal.valueOf(6)),
            avkortAndelerSomIkkegjelderAFtil0, avkortAndelerAndelsmessigOgFastsettBrukersAndel);

        //FP_BR_29.8.1 For alle beregningsgrunnlagsandeler som gjelder arbeidsforhold, fastsett avkortet refusjon pr andel
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetBGOver6GNårRefusjonUnder6G =
            rs.beregningsRegel(ID, BESKRIVELSE, new FastsettAvkortetRefusjonPrAndel(), erTotaltBGFraArbeidforholdStørreEnn6G);


        return fastsettAvkortetBGOver6GNårRefusjonUnder6G;
    }

    private Specification<BeregningsgrunnlagPeriode> opprettRegelFastsettUtbetalingsbeløpTilBruker() {
        return new RegelFastsettUtbetalingsbeløpTilBruker().getSpecification();
    }
}
