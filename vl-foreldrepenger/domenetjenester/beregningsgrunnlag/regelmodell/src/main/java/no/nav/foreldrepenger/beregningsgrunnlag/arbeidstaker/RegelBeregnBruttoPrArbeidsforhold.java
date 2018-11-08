package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.specification.Specification;

public class RegelBeregnBruttoPrArbeidsforhold extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR 14.1";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {

        ServiceArgument arg = getServiceArgument();
        if (arg == null || ! (arg.getVerdi() instanceof BeregningsgrunnlagPrArbeidsforhold)) {
            throw new IllegalStateException("Utviklerfeil: Arbeidsforhold må angis som parameter");
        }
        BeregningsgrunnlagPrArbeidsforhold arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold)arg.getVerdi();

        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        //FP_BR 15.6 Beregn naturalytelse -= (verdi x 12)
        //FP_BR 15.5 Naturalytelse som tilkommer ved start av perioden eller i tidligere perioder?
        Specification<BeregningsgrunnlagPeriode> tilkommetNaturalYtelse =
            rs.beregningHvisRegel(new SjekkOmTilkommetNaturalytelse(arbeidsforhold),
                new BeregnPrArbeidsforholdNaturalytelseTilkommet(arbeidsforhold), new Beregnet());

        //FP_BR 15.4 Beregn naturalytelse
        Specification<BeregningsgrunnlagPeriode> bortfallNaturalYtelse =
            rs.beregningsRegel(BeregnPrArbeidsforholdNaturalytelseBortfalt.ID, BeregnPrArbeidsforholdNaturalytelseBortfalt.BESKRIVELSE,
                new BeregnPrArbeidsforholdNaturalytelseBortfalt(arbeidsforhold), tilkommetNaturalYtelse);

        //FP_BR 15.3 Bortfall av naturalytelse ved start av perioden eller i tidligere periode?
        Specification<BeregningsgrunnlagPeriode> beregnBortfallAvNaturalYtelse =
            rs.beregningHvisRegel(new SjekkOmBortfallAvNaturalytelse(arbeidsforhold), bortfallNaturalYtelse, tilkommetNaturalYtelse);

        // FP_BR 15.2 Brutto pr periode_type = inntektsmelding sats * 12
        Specification<BeregningsgrunnlagPeriode> beregnPrArbeidsforholdFraInntektsmelding =
            rs.beregningsRegel(BeregnPrArbeidsforholdFraInntektsmelding.ID, BeregnPrArbeidsforholdFraInntektsmelding.BESKRIVELSE,
                new BeregnPrArbeidsforholdFraInntektsmelding(arbeidsforhold), beregnBortfallAvNaturalYtelse);

        // FB_BR 14.3 Brutto pr periodetype = snitt av fastsatte inntekter av A-ordning * 12
        Specification<BeregningsgrunnlagPeriode> beregnPrArbeidsforholdFraAOrdningen = new BeregnPrArbeidsforholdFraAOrdningen(arbeidsforhold);


        // FP_BR 15.6 Rapportert inntekt = manuelt fastsatt månedsinntekt * 12
        Specification<BeregningsgrunnlagPeriode> beregnÅrsinntektVedManuellFastsettelse =
            rs.beregningsRegel(BeregnRapportertInntektVedManuellFastsettelse.ID, BeregnRapportertInntektVedManuellFastsettelse.BESKRIVELSE,
            new BeregnRapportertInntektVedManuellFastsettelse(arbeidsforhold), new Beregnet());

        // FP_BR 15.1 Foreligger inntektsmelding?
        Specification<BeregningsgrunnlagPeriode> sjekkOmInntektsmeldingForeligger =
                rs.beregningHvisRegel(new SjekkOmInntektsmeldingForeligger(arbeidsforhold),
                        beregnPrArbeidsforholdFraInntektsmelding, beregnPrArbeidsforholdFraAOrdningen).medScope(arg);

        // FP_BR 15.5 Har saksbehandler fastsatt månedsinntekt manuelt?
        Specification<BeregningsgrunnlagPeriode> manueltFastsattInntekt = rs.beregningHvisRegel(
            new SjekkHarSaksbehandlerSattInntektManuelt(arbeidsforhold), beregnÅrsinntektVedManuellFastsettelse, sjekkOmInntektsmeldingForeligger);


        // FP_BR 14.9 Er bruker nyoppstartet frilanser?

        Specification<BeregningsgrunnlagPeriode> sjekkOmNyoppstartetFrilanser =
            rs.beregningHvisRegel(new SjekkOmFrilansInntektErFastsattAvSaksbehandler(arbeidsforhold),
               new BeregnGrunnlagNyoppstartetFrilanser(arbeidsforhold), beregnPrArbeidsforholdFraAOrdningen);

        // FP_BR 14.1 Er bruker arbeidstaker?

        Specification<BeregningsgrunnlagPeriode> fastsettBruttoBeregningsgrunnlag =
                rs.beregningHvisRegel(new SjekkOmBrukerErArbeidstaker(arbeidsforhold), manueltFastsattInntekt, sjekkOmNyoppstartetFrilanser);


        return fastsettBruttoBeregningsgrunnlag;
    }
}
