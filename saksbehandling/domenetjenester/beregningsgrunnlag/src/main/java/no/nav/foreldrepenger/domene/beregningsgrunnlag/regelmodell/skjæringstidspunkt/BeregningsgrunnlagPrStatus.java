package no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.skjæringstidspunkt;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt.Arbeidsforhold;

public class BeregningsgrunnlagPrStatus {
    private AktivitetStatus aktivitetStatus;
    private List<Arbeidsforhold> arbeidsforholdList = new ArrayList<>();

    public BeregningsgrunnlagPrStatus(AktivitetStatus aktivitetStatus){
        this.aktivitetStatus = aktivitetStatus;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public List<Arbeidsforhold> getArbeidsforholdList() {
        return arbeidsforholdList;
    }
}
