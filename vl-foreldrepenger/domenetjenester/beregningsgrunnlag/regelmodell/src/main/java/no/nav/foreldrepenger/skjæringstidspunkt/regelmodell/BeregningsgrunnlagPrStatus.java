package no.nav.foreldrepenger.skjæringstidspunkt.regelmodell;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

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
