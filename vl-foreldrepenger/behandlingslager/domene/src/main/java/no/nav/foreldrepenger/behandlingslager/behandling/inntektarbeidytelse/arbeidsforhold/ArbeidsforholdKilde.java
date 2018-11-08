package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

public enum ArbeidsforholdKilde {
    AAREGISTERET("AA-Registeret"),
    INNTEKTSKOMPONENTEN("Inntekt"),
    INNTEKTSMELDING("Inntektsmelding"),
    SAKSBEHANDLER("Saksbehandler");

    private String navn;

    ArbeidsforholdKilde(String navn) {
        this.navn = navn;
    }

    public String getNavn() {
        return navn;
    }
}
