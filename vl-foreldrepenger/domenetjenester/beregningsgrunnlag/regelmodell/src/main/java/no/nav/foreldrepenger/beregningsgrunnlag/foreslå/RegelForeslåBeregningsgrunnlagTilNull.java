package no.nav.foreldrepenger.beregningsgrunnlag.foreslå;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagTilNull.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=216009135")
public class RegelForeslåBeregningsgrunnlagTilNull extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_NULL";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        ServiceArgument arg = getServiceArgument();
        if (arg == null || ! (arg.getVerdi() instanceof AktivitetStatusMedHjemmel)) {
            throw new IllegalStateException("Utviklerfeil: AktivitetStatus må angis som parameter");
        }
        AktivitetStatus status = ((AktivitetStatusMedHjemmel)arg.getVerdi()).getAktivitetStatus();
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR NULL Sett brutto BG til 0
        Specification<BeregningsgrunnlagPeriode> settTilNull = rs.beregningsRegel("FP_BR NULL", "Beregn brutto beregingsgrunnlag for ukjent status",
            new FastsettTilNull(status), new Beregnet());


        return settTilNull;
    }
}
