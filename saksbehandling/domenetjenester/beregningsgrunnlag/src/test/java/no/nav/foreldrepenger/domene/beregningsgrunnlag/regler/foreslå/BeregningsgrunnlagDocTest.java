package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå;

import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.BeregningsgrunnlagScenario.settoppMånedsinntekter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.junit.Test;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Inntektskilde;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class BeregningsgrunnlagDocTest {

    @Test
    public void test_documentation() throws Exception {
        String orgnr = "987654321";
        LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt,
                Arrays.asList(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL),
                Arrays.asList(arbeidsforhold), Arrays.asList(månedsinntekt.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

        Specification<BeregningsgrunnlagPeriode> beregning = new RegelForeslåBeregningsgrunnlag(grunnlag).getSpecification();

        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }
}
