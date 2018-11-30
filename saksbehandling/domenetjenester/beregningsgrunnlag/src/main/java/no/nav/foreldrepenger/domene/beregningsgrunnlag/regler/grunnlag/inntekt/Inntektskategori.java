package no.nav.foreldrepenger.domene.beregningsgrunnlag.regler.grunnlag.inntekt;

import no.nav.foreldrepenger.domene.beregningsgrunnlag.regelmodell.AktivitetStatus;

public enum Inntektskategori {
    ARBEIDSTAKER(AktivitetStatus.ATFL),
    FRILANSER(AktivitetStatus.ATFL),
    SELVSTENDIG_NÆRINGSDRIVENDE(AktivitetStatus.SN),
    DAGPENGER(AktivitetStatus.DP),
    ARBEIDSAVKLARINGSPENGER(AktivitetStatus.AAP),
    SJØMANN(AktivitetStatus.ATFL),
    DAGMAMMA(AktivitetStatus.SN),
    JORDBRUKER(AktivitetStatus.SN),
    FISKER(AktivitetStatus.SN),
    ARBEIDSTAKER_UTEN_FERIEPENGER(AktivitetStatus.ATFL),
    UDEFINERT(AktivitetStatus.UDEFINERT);

    private AktivitetStatus aktivitetStatus;

    Inntektskategori(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }
}
