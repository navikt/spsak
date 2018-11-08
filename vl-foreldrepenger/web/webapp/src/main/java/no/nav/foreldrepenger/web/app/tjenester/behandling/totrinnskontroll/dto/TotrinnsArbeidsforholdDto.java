package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType;

public class TotrinnsArbeidsforholdDto {
    private String navn;
    private String organisasjonsnummer;
    private String arbeidsforholdId;
    private ArbeidsforholdHandlingType arbeidsforholdHandlingType;

    public TotrinnsArbeidsforholdDto(String navn, String organisasjonsnummer, String arbeidsforholdId, ArbeidsforholdHandlingType handling) {
        this.navn = navn;
        this.organisasjonsnummer = organisasjonsnummer;
        this.arbeidsforholdId = arbeidsforholdId;
        this.arbeidsforholdHandlingType = handling;
    }

    public String getNavn() { return navn; }

    public String getOrganisasjonsnummer() { return organisasjonsnummer; }

    public String getArbeidsforholdId() { return arbeidsforholdId; }

    public ArbeidsforholdHandlingType getArbeidsforholdHandlingType() {
        return arbeidsforholdHandlingType;
    }
}
