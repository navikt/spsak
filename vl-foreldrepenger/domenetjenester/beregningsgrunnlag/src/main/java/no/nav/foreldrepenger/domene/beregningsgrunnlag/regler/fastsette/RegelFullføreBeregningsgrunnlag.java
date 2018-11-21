package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette;

import java.math.BigDecimal;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker.AvslagUnderEnHalvG;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.arbeidstaker.RegelFastsettUtenAvkortingATFL;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.avkorting.RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.refusjon.FastsettMaksimalRefusjon;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.refusjon.over6g.RegelFastsettAvkortetVedRefusjonOver6G;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.reduksjon.FastsettDagsatsPrAndel;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.reduksjon.ReduserBeregningsgrunnlag;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFullføreBeregningsgrunnlag extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29";

    public RegelFullføreBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR_6.1 18. Fastsett Reduksjon for hver beregningsgrunnlagsandel og totalt beregningrunnlag etter reduksjon
        // FP_BR_6.2 19. Fastsett dagsats pr beregningsgrunnlagsandel og totalt
        Specification<BeregningsgrunnlagPeriode> fastsettReduksjon = rs.beregningsRegel(ReduserBeregningsgrunnlag.ID, ReduserBeregningsgrunnlag.BESKRIVELSE, new ReduserBeregningsgrunnlag(), new FastsettDagsatsPrAndel());

        // 13 Fastsett avkortet BG når refusjon over 6G
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetVedRefusjonOver6G = rs.beregningsRegel(
                RegelFastsettAvkortetVedRefusjonOver6G.ID,
                RegelFastsettAvkortetVedRefusjonOver6G.BESKRIVELSE,
                new RegelFastsettAvkortetVedRefusjonOver6G(regelmodell).getSpecification(),
                fastsettReduksjon);

        // 8. Fastsett avkortet BG når refusjon under 6G
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetVedRefusjonUnder6G = rs.beregningsRegel(
                RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G.ID,
                RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G.BESKRIVELSE,
                new RegelFastsettAvkortetBGOver6GNårRefusjonUnder6G(regelmodell).getSpecification(),
                fastsettReduksjon);

        // FP_BR_29.7 7. Sjekk om summen av maksimal refusjon overstiger 6G
        Specification<BeregningsgrunnlagPeriode> sjekkMaksimaltRefusjonskrav = rs.beregningHvisRegel(new SjekkSumMaxRefusjonskravStørreEnn6G(), fastsettAvkortetVedRefusjonOver6G, fastsettAvkortetVedRefusjonUnder6G);

        // 6. Fastsett uten avkorting
        Specification<BeregningsgrunnlagPeriode> fastsettUtenAvkorting = rs.beregningsRegel("FP_BR_29.6", "Fastsett BG uten avkorting", new RegelFastsettUtenAvkortingATFL().getSpecification(), fastsettReduksjon);

        // FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
        Specification<BeregningsgrunnlagPeriode> beregnEventuellAvkorting = rs.beregningHvisRegel(new SjekkBeregningsgrunnlagStørreEnn6G(), sjekkMaksimaltRefusjonskrav, fastsettUtenAvkorting);

        // FP_BR_29.3 3. For hver beregningsgrunnlagsandel: Fastsett Refusjonskrav for beregnings-grunnlagsandel
        Specification<BeregningsgrunnlagPeriode> fastsettMaksimalRefusjon = rs.beregningsRegel(FastsettMaksimalRefusjon.ID, FastsettMaksimalRefusjon.BESKRIVELSE, new FastsettMaksimalRefusjon(), beregnEventuellAvkorting);

        // FP_VK_32.2 2. Opprett regelmerknad (avslag)
        Specification<BeregningsgrunnlagPeriode> avslagUnderEnHalvG = new AvslagUnderEnHalvG();

        // FP_VK_32.1 1. Brutto BG > 0,5G ?
        Specification<BeregningsgrunnlagPeriode> fastsettBeregningsgrunnlag = rs.beregningHvisRegel(new SjekkBeregningsgrunnlagMindreEnn(new BigDecimal("0.5")),
                avslagUnderEnHalvG, fastsettMaksimalRefusjon);

        return fastsettBeregningsgrunnlag;
    }
}
