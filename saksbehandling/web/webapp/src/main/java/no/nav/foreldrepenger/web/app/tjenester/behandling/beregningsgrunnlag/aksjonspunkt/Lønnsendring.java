package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;

class Lønnsendring {
    private BeregningsgrunnlagPrStatusOgAndel andel;
    private Integer gammelArbeidsinntekt;
    private Integer nyArbeidsinntekt;

    Lønnsendring(BeregningsgrunnlagPrStatusOgAndel andel, Integer gammelArbeidsinntekt, Integer nyArbeidsinntekt) {
        this.andel = andel;
        this.gammelArbeidsinntekt = gammelArbeidsinntekt;
        this.nyArbeidsinntekt = nyArbeidsinntekt;
    }

    BeregningsgrunnlagPrStatusOgAndel getAndel() {
        return andel;
    }

    Integer getGammelArbeidsinntekt() {
        return gammelArbeidsinntekt;
    }

    Integer getNyArbeidsinntekt() {
        return nyArbeidsinntekt;
    }
}
