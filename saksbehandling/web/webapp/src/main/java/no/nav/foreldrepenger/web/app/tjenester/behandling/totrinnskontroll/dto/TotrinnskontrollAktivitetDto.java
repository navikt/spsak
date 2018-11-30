package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto;

public class TotrinnskontrollAktivitetDto {

    private String aktivitetType;
    private Boolean erEndring;
    private String arbeidsgiverNavn;
    private String orgnr;
    private boolean godkjent;

    public TotrinnskontrollAktivitetDto() {
        //Tom
    }

    public TotrinnskontrollAktivitetDto(String aktivitetType, Boolean erEndring, String arbeidsgiverNavn, String orgnr, boolean godkjent) {
        this.aktivitetType = aktivitetType;
        this.erEndring = erEndring;
        this.arbeidsgiverNavn = arbeidsgiverNavn;
        this.orgnr = orgnr;
        this.godkjent = godkjent;
    }

    public String getAktivitetType() {
        return aktivitetType;
    }

    public Boolean getErEndring() {
        return erEndring;
    }

    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }

    public String getOrgnr() {
        return orgnr;
    }

    public boolean isGodkjent() {
        return godkjent;
    }
}
