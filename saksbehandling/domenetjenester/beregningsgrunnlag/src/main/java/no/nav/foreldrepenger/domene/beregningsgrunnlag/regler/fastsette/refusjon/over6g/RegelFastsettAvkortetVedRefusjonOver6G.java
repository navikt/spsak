package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.fastsette.refusjon.over6g;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsettAvkortetVedRefusjonOver6G implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29.13";
    public static final String BESKRIVELSE = "Fastsett avkortet BG når refusjon over 6G";
    private BeregningsgrunnlagPeriode regelmodell;

    public RegelFastsettAvkortetVedRefusjonOver6G(BeregningsgrunnlagPeriode regelmodell) {
        super();
        this.regelmodell = regelmodell;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        BeregningsgrunnlagPrStatus bgpsa = regelmodell.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (bgpsa == null) {
            return new Beregnet();
        }

        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        Specification<BeregningsgrunnlagPeriode> fastsettBrukersAndelerTilNull = new FastsettBrukersAndelerTilNull();

        int antallKjøringer = bgpsa.getArbeidsforhold().size();
        List<Specification<BeregningsgrunnlagPeriode>> prArbeidsforhold = new ArrayList<>();
        for (int nr = 1; nr <= antallKjøringer; nr++) {
            prArbeidsforhold.add(opprettRegelBeregnRefusjonPrArbeidsforhold());
        }
        Specification<BeregningsgrunnlagPeriode> fastsettAvkortetBeregningsgrunnlag =
                rs.beregningsRegel(ID, BESKRIVELSE, prArbeidsforhold, fastsettBrukersAndelerTilNull);

        return fastsettAvkortetBeregningsgrunnlag;
    }

    private Specification<BeregningsgrunnlagPeriode> opprettRegelBeregnRefusjonPrArbeidsforhold() {
        return new RegelBeregnRefusjonPrArbeidsforhold().getSpecification();
    }
}
