package no.nav.foreldrepenger.beregningsgrunnlag.selvstendig;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.Grunnbeløp;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class BeregningsgrunnlagDocTest {

    private static final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    private BeregningsgrunnlagPeriode regelmodell;

    @Before
    public void setup() {
        regelmodell = BeregningsgrunnlagPeriode.builder()
                .medPeriode(Periode.of(skjæringstidspunkt, null))
                .medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
                        .medAktivitetStatus(AktivitetStatus.SN)
                        .medAndelNr(1L)
                        .build())
                .build();
        Beregningsgrunnlag.builder()
                .medSkjæringstidspunkt(skjæringstidspunkt)
                .medInntektsgrunnlag(new Inntektsgrunnlag())
                .medAktivitetStatuser(Arrays.asList(new AktivitetStatusMedHjemmel(AktivitetStatus.SN, null)))
                .medBeregningsgrunnlagPeriode(regelmodell)
                .medGrunnbeløpSatser(Arrays.asList(new Grunnbeløp(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2099,  Month.DECEMBER,  31), 90000L, 90000L)))
                .build();
    }

    @Test
    public void test_documentation_beregningsgrunnlagSN() throws Exception {
        Specification<BeregningsgrunnlagPeriode> beregning = new RegelBeregningsgrunnlagSN().getSpecification();
        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }
}
