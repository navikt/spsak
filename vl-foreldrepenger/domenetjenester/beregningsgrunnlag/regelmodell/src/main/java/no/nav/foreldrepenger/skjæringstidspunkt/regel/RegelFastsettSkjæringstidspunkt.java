package no.nav.foreldrepenger.skjæringstidspunkt.regel;

import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelFastsettSkjæringstidspunkt.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=234395762")
public class RegelFastsettSkjæringstidspunkt implements RuleService<AktivitetStatusModell> {

    static final String ID = "FP_BR_21";

    @Override
    public Evaluation evaluer(AktivitetStatusModell regelmodell) {
        return getSpecification().evaluate(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<AktivitetStatusModell> getSpecification() {

        Ruleset<AktivitetStatusModell> rs = new Ruleset<>();

//      FP_BR 21.5 Skjæringstidspunkt for beregning = første dag etter siste aktivitetsdag før skjæringstidspunkt for opptjening

        Specification<AktivitetStatusModell> førsteDagEtterAktivitet = new FastsettSkjæringstidspunktEtterAktivitet();

//      FP_BR 21.6 Skjæringstidspunkt for beregning = skjæringstidspunkt for opptjening

        Specification<AktivitetStatusModell> likSkjæringstidspunktForOpptjening = new FastsettSkjæringstidspunktLikOpptjening();

//      FP_BR 21.1 Er tom-dato for siste aktivitet dagen før skjæringstidspunkt for opptjening?

        Specification<AktivitetStatusModell> sjekkOmAktivitetRettFørOpptjening =
                rs.beregningHvisRegel(new SjekkOmAktivitetRettFørOpptjening(), likSkjæringstidspunktForOpptjening, førsteDagEtterAktivitet);

//      FP_BR 21.X Fjern militær eller obligatorisk sivilforsvarstjeneste fra listen over aktiviteter
        Specification<AktivitetStatusModell> fjernMilitærAktivitet =
                rs.beregningsRegel("FP_BR 21.X", "Fjern militær",
                    new FastsettFjernMilitærAktivitet(), sjekkOmAktivitetRettFørOpptjening);

//      FP_BR 21.3 Har tjenesten vart mer enn 28 dager eller er tjenesten ment å vare mer enn 28 dager?

        Specification<AktivitetStatusModell> sjekkMilitærVarighet =
                rs.beregningHvisRegel(new SjekkMilitærVarighet(), sjekkOmAktivitetRettFørOpptjening, fjernMilitærAktivitet);

//      FP_BR 21.2 Inngår aktiviteten militær eller obligatorisk sivilforsvarstjeneste som siste aktivitet fra opptjening aktivitetsperioden?

        Specification<AktivitetStatusModell> startFastsettSkjæringstidspunktForBeregning =
                rs.beregningHvisRegel(new SjekkOmMilitærAktivitet(), sjekkMilitærVarighet, sjekkOmAktivitetRettFørOpptjening);

//      Start fastsett skjæringstidspunkt for beregning

        return startFastsettSkjæringstidspunktForBeregning;
    }
}
