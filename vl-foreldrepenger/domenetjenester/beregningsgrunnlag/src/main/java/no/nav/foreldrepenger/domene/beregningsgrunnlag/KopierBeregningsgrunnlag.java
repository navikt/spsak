package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;

class KopierBeregningsgrunnlag {

    private KopierBeregningsgrunnlag() {
        // skjuler default public constructor
    }

    static void kopierOverstyrteVerdier(Beregningsgrunnlag gammelBG, Beregningsgrunnlag nyBG) {
        List<BeregningsgrunnlagPeriode> gamlePerioder = gammelBG.getBeregningsgrunnlagPerioder();
        nyBG.getBeregningsgrunnlagPerioder().forEach(nyPeriode -> kopierOverstyrteVerdierFraPeriode(gamlePerioder, nyPeriode));
    }

    private static void kopierOverstyrteVerdierFraPeriode(List<BeregningsgrunnlagPeriode> gamlePerioder, BeregningsgrunnlagPeriode nyPeriode) {
        Set<BeregningsgrunnlagPeriode> matchendePerioder = gamlePerioder.stream()
            .filter(gammelPeriode -> gammelPeriode.getPeriode().overlapper(nyPeriode.getPeriode()))
            .collect(Collectors.toSet());
        if (matchendePerioder.size() != 1) {
            return;
        }
        BeregningsgrunnlagPeriode gammelPeriode = matchendePerioder.iterator().next();
        List<BeregningsgrunnlagPrStatusOgAndel> gamleAndeler = gammelPeriode.getBeregningsgrunnlagPrStatusOgAndelList();
        nyPeriode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(nyAndel -> kopierOverstyrteVerdierFraAndel(gamleAndeler, nyAndel));
    }

    private static void kopierOverstyrteVerdierFraAndel(List<BeregningsgrunnlagPrStatusOgAndel> gamleAndeler, BeregningsgrunnlagPrStatusOgAndel nyAndel) {
        Set<BeregningsgrunnlagPrStatusOgAndel> matchendeAndeler = gamleAndeler.stream()
            .filter(gammelAndel -> gammelAndel.equals(nyAndel))
            .collect(Collectors.toSet());
        if (matchendeAndeler.size() != 1) {
            return;
        }
        BeregningsgrunnlagPrStatusOgAndel gammelAndel = matchendeAndeler.iterator().next();
        BeregningsgrunnlagPrStatusOgAndel.Builder builder = BeregningsgrunnlagPrStatusOgAndel.builder(nyAndel)
            .medBeregnetPrÅr(gammelAndel.getBeregnetPrÅr())
            .medOverstyrtPrÅr(gammelAndel.getOverstyrtPrÅr())
            .medNyIArbeidslivet(gammelAndel.getNyIArbeidslivet())
            .medFastsattAvSaksbehandler(gammelAndel.getFastsattAvSaksbehandler())
            .medLagtTilAvSaksbehandler(gammelAndel.getLagtTilAvSaksbehandler())
            .medInntektskategori(gammelAndel.getInntektskategori())
            .medBesteberegningPrÅr(gammelAndel.getBesteberegningPrÅr());
        if (erArbeidsforholdPåGammelAndelOverstyrt(gammelAndel)) {
            builder
                .medBGAndelArbeidsforhold(kopierBGAndelArbeidsforhold(nyAndel, gammelAndel));
        }
    }

    private static boolean erArbeidsforholdPåGammelAndelOverstyrt(BeregningsgrunnlagPrStatusOgAndel gammelAndel) {
        return gammelAndel.getBgAndelArbeidsforhold()
            .filter(bga -> bga.getErTidsbegrensetArbeidsforhold() != null || bga.erLønnsendringIBeregningsperioden() != null).isPresent();
    }

    private static BGAndelArbeidsforhold.Builder kopierBGAndelArbeidsforhold(BeregningsgrunnlagPrStatusOgAndel nyAndel, BeregningsgrunnlagPrStatusOgAndel gammelAndel) {
                return BGAndelArbeidsforhold.builder(nyAndel.getBgAndelArbeidsforhold().orElse(null))
                    .medTidsbegrensetArbeidsforhold(gammelAndel.getBgAndelArbeidsforhold()
                        .map(BGAndelArbeidsforhold::getErTidsbegrensetArbeidsforhold)
                        .orElse(null))
                    .medLønnsendringIBeregningsperioden(gammelAndel.getBgAndelArbeidsforhold()
                        .map(BGAndelArbeidsforhold::erLønnsendringIBeregningsperioden)
                        .orElse(null));

    }
}
