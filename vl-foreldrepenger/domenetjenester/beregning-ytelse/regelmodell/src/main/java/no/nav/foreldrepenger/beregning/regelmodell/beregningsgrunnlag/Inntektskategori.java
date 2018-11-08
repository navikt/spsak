package no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag;

public enum Inntektskategori {
    ARBEIDSTAKER,
    FRILANSER,
    SELVSTENDIG_NÆRINGSDRIVENDE,
    DAGPENGER,
    ARBEIDSAVKLARINGSPENGER,
    SJØMANN,
    DAGMAMMA,
    JORDBRUKER,
    FISKER,
    ARBEIDSTAKER_UTEN_FERIEPENGER,
    UDEFINERT;

    public boolean erArbeidstakerEllerSjømann() {
        return this == ARBEIDSTAKER || this == SJØMANN;
    }
}
