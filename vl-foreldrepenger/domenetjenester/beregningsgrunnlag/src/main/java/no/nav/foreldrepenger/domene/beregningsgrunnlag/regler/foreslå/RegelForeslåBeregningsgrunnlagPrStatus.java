package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.foreslå;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.IkkeBeregnet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.kombinasjon.RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.selvstendig.RegelBeregningsgrunnlagSN;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.RegelForeslåBeregningsgrunnlagTY;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.ytelse.dagpengerelleraap.RegelFastsettBeregningsgrunnlagDPellerAAP;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.Specification;


/**
 * Det mangler dokumentasjon
 */

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagPrStatus.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=180066764")
public class RegelForeslåBeregningsgrunnlagPrStatus extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "BG-PR-STATUS";

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        ServiceArgument arg = getServiceArgument();
        if (arg == null || !(arg.getVerdi() instanceof AktivitetStatusMedHjemmel)) {
            throw new IllegalStateException("Utviklerfeil: AktivitetStatus må angis som parameter");
        }
        if (regelmodell.getBeregningsgrunnlagPrStatus().isEmpty()) {
            return new IkkeBeregnet(new RuleReasonRefImpl("x", "y"));
        }
        AktivitetStatus aktivitetStatus = ((AktivitetStatusMedHjemmel) arg.getVerdi()).getAktivitetStatus();
        if (AktivitetStatus.ATFL.equals(aktivitetStatus)) {
            return new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification().medScope(arg);
        } else if (AktivitetStatus.SN.equals(aktivitetStatus)) {
            return new RegelBeregningsgrunnlagSN().getSpecification().medScope(arg);
        } else if (AktivitetStatus.ATFL_SN.equals(aktivitetStatus)) {
            RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN regelFastsetteBeregningsgrunnlagForKombinasjonATFLSN = new RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSN(regelmodell);
            return regelFastsetteBeregningsgrunnlagForKombinasjonATFLSN.getSpecification().medScope(arg);
        } else if (AktivitetStatus.AAP.equals(aktivitetStatus) || AktivitetStatus.DP.equals(aktivitetStatus)) {
            RegelFastsettBeregningsgrunnlagDPellerAAP regelFastsettBeregningsgrunnlagDPellerAAP = new RegelFastsettBeregningsgrunnlagDPellerAAP();
            return regelFastsettBeregningsgrunnlagDPellerAAP.getSpecification().medScope(arg);
        } else if (AktivitetStatus.TY.equals(aktivitetStatus)) {
            RegelForeslåBeregningsgrunnlagTY regelForeslåBeregningsgrunnlagTY = new RegelForeslåBeregningsgrunnlagTY();
            return regelForeslåBeregningsgrunnlagTY.getSpecification().medScope(arg);
        }
        return new RegelForeslåBeregningsgrunnlagTilNull().medServiceArgument(arg).getSpecification();
    }
}
