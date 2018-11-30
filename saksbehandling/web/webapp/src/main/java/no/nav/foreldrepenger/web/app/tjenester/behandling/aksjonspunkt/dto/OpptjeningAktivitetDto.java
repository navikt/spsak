package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public class OpptjeningAktivitetDto {
    @NotNull
    @ValidKodeverk
    private OpptjeningAktivitetType aktivitetType;

    private LocalDate originalFom;

    private LocalDate originalTom;

    @NotNull
    private LocalDate opptjeningFom;

    @NotNull
    private LocalDate opptjeningTom;

    @Pattern(regexp = "\\d{9}|\\d{13}")
    private String oppdragsgiverOrg;

    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String arbeidsforholdRef;

    @Min(0)
    @Max(100)
    @Digits(integer = 3, fraction = 2)
    private BigDecimal stillingsandel;

    private LocalDate naringRegistreringsdato;

    private Boolean erManueltOpprettet;

    private Boolean erGodkjent;

    private Boolean erEndret;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    public OpptjeningAktivitetDto() {//NOSONAR
        // trengs for deserialisering av JSON
    }

    public LocalDate getOriginalFom() {
        return originalFom;
    }

    public void setOriginalFom(LocalDate originalFom) {
        this.originalFom = originalFom;
    }

    public LocalDate getOriginalTom() {
        return originalTom;
    }

    public void setOriginalTom(LocalDate originalTom) {
        this.originalTom = originalTom;
    }

    public OpptjeningAktivitetType getAktivitetType() {
        return aktivitetType;
    }

    public void setAktivitetType(OpptjeningAktivitetType aktivitetType) {
        this.aktivitetType = aktivitetType;
    }

    public LocalDate getOpptjeningFom() {
        return opptjeningFom;
    }

    public void setOpptjeningFom(LocalDate opptjeningFom) {
        this.opptjeningFom = opptjeningFom;
    }

    public LocalDate getOpptjeningTom() {
        return opptjeningTom;
    }

    public void setOpptjeningTom(LocalDate opptjeningTom) {
        this.opptjeningTom = opptjeningTom;
    }

    public String getOppdragsgiverOrg() {
        return oppdragsgiverOrg;
    }

    public void setOppdragsgiverOrg(String oppdragsgiverOrg) {
        this.oppdragsgiverOrg = oppdragsgiverOrg;
    }

    public LocalDate getNaringRegistreringsdato() {
        return naringRegistreringsdato;
    }

    public void setNaringRegistreringsdato(LocalDate naringRegistreringsdato) {
        this.naringRegistreringsdato = naringRegistreringsdato;
    }

    public BigDecimal getStillingsandel() {
        return stillingsandel;
    }

    public void setStillingsandel(BigDecimal stillingsandel) {
        this.stillingsandel = stillingsandel;
    }

    public Boolean getErGodkjent() {
        return erGodkjent;
    }

    public void setErGodkjent(Boolean erGodkjent) {
        this.erGodkjent = erGodkjent;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public Boolean getErManueltOpprettet() {
        return erManueltOpprettet;
    }

    public void setErManueltOpprettet(Boolean erManueltOpprettet) {
        this.erManueltOpprettet = erManueltOpprettet;
    }

    public Boolean getErEndret() {
        return erEndret;
    }

    public void setErEndret(Boolean erEndret) {
        this.erEndret = erEndret;
    }

    public String getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public void setArbeidsforholdRef(String arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef;
    }
}
