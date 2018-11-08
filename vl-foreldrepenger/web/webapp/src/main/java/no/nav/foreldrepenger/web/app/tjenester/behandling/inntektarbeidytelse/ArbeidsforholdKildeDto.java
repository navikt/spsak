package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

class ArbeidsforholdKildeDto {
    private String navn;

    public ArbeidsforholdKildeDto(String navn) {
        this.navn = navn;
    }

    public String getNavn() {

        return navn;
    }
}
