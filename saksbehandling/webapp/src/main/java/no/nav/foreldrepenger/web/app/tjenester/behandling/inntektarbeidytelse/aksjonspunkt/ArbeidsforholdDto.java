package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.aksjonspunkt;


import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.util.InputValideringRegex;

public class ArbeidsforholdDto {

    // AktørId (13-tall) for person-arbeidsgiver, og orgnr (9-tall) for virksomhet
    @Pattern(regexp = "\\d{9}|\\d{13}")
    private String arbeidsgiverIdentifikator;

    @Size(max = 100)
    private String id;

    @Size(max = 100)
    private String arbeidsforholdId;
    @Size(max = 100)
    private String erstatterArbeidsforholdId;
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String beskrivelse;
    @Min(0)
    @Max(100)
    @Digits(integer = 3, fraction = 2)
    private BigDecimal stillingsprosent;
    private Boolean brukArbeidsforholdet;
    private Boolean fortsettBehandlingUtenInntektsmelding;
    private Boolean erNyttArbeidsforhold;

    public ArbeidsforholdDto() {//NOSONAR
        // trengs for deserialisering av JSON
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public void setArbeidsgiverIdentifikator(String arbeidsgiverIdentifikator) {
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public void setStillingsprosent(BigDecimal stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }

    public Boolean getBrukArbeidsforholdet() {
        return brukArbeidsforholdet;
    }

    public void setBrukArbeidsforholdet(Boolean brukArbeidsforholdet) {
        this.brukArbeidsforholdet = brukArbeidsforholdet;
    }

    public Boolean getFortsettBehandlingUtenInntektsmelding() {
        return fortsettBehandlingUtenInntektsmelding;
    }

    public void setFortsettBehandlingUtenInntektsmelding(Boolean fortsettBehandlingUtenInntektsmelding) {
        this.fortsettBehandlingUtenInntektsmelding = fortsettBehandlingUtenInntektsmelding;
    }

    public String getErstatterArbeidsforholdId() {
        return erstatterArbeidsforholdId;
    }

    public void setErstatterArbeidsforholdId(String erstatterArbeidsforholdId) {
        this.erstatterArbeidsforholdId = erstatterArbeidsforholdId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getErNyttArbeidsforhold() {
        return erNyttArbeidsforhold;
    }

    public void setErNyttArbeidsforhold(Boolean erNyttArbeidsforhold) {
        this.erNyttArbeidsforhold = erNyttArbeidsforhold;
    }
}
