package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.vedtak.util.FPDateUtil;

public class ArbeidsforholdWrapper {

    private String navn;
    private String arbeidsgiverIdentifikator;
    private String personArbeidsgiverFnr;
    private String arbeidsforholdId;
    private LocalDate fomDato = LocalDate.now(FPDateUtil.getOffset());
    private LocalDate tomDato;
    private ArbeidsforholdKilde kilde;
    private LocalDate mottattDatoInntektsmelding;
    private String beskrivelse;
    private BigDecimal stillingsprosent;
    private Boolean brukArbeidsforholdet;
    private Boolean fortsettBehandlingUtenInntektsmelding;
    private Boolean erNyttArbeidsforhold;
    private Boolean erEndret;
    private Boolean erSlettet;
    private String erstatterArbeidsforhold;
    private Boolean harErsattetEttEllerFlere;
    private Boolean ikkeRegistrertIAaRegister;
    private boolean harAksjonspunkt = false;
    private boolean vurderOmSkalErstattes = false;

    public boolean isHarAksjonspunkt() {
        return harAksjonspunkt;
    }

    public void setHarAksjonspunkt(boolean harAksjonspunkt) {
        this.harAksjonspunkt = harAksjonspunkt;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public void setArbeidsgiverIdentifikator(String arbeidsgiverIdentifikator) {
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
    }

    public LocalDate getFomDato() {
        return fomDato;
    }

    public void setFomDato(LocalDate fomDato) {
        if (fomDato != null) {
            this.fomDato = fomDato;
        }
    }

    public LocalDate getTomDato() {
        return tomDato;
    }

    public void setTomDato(LocalDate tomDato) {
        this.tomDato = tomDato;
    }

    public ArbeidsforholdKilde getKilde() {
        return kilde;
    }

    public void setKilde(ArbeidsforholdKilde kilde) {
        this.kilde = kilde;
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

    public LocalDate getMottattDatoInntektsmelding() {
        return mottattDatoInntektsmelding;
    }

    public void setMottattDatoInntektsmelding(LocalDate mottattDatoInntektsmelding) {
        this.mottattDatoInntektsmelding = mottattDatoInntektsmelding;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public Boolean getErNyttArbeidsforhold() {
        return erNyttArbeidsforhold;
    }

    public void setErNyttArbeidsforhold(Boolean erNyttArbeidsforhold) {
        this.erNyttArbeidsforhold = erNyttArbeidsforhold;
    }

    public Boolean getErEndret() {
        return erEndret;
    }

    public void setErEndret(Boolean erEndret) {
        this.erEndret = erEndret;
    }

    public Boolean getErSlettet() {
        return erSlettet;
    }

    public void setErSlettet(Boolean erSlettet) {
        this.erSlettet = erSlettet;
    }

    public String getErstatterArbeidsforhold() {
        return erstatterArbeidsforhold;
    }

    public void setErstatterArbeidsforhold(String erstatterArbeidsforhold) {
        this.erstatterArbeidsforhold = erstatterArbeidsforhold;
    }

    public Boolean getHarErsattetEttEllerFlere() {
        return harErsattetEttEllerFlere;
    }

    public void setHarErsattetEttEllerFlere(Boolean harErsattetEttEllerFlere) {
        this.harErsattetEttEllerFlere = harErsattetEttEllerFlere;
    }

    public Boolean getIkkeRegistrertIAaRegister() {
        return ikkeRegistrertIAaRegister;
    }

    public void setIkkeRegistrertIAaRegister(Boolean ikkeRegistrertIAaRegister) {
        this.ikkeRegistrertIAaRegister = ikkeRegistrertIAaRegister;
    }

    public boolean getVurderOmSkalErstattes() {
        return vurderOmSkalErstattes;
    }

    public void setVurderOmSkalErstattes(boolean vurderOmSkalErstattes) {
        this.vurderOmSkalErstattes = vurderOmSkalErstattes;
    }

    public String getPersonArbeidsgiverFnr() {
        return personArbeidsgiverFnr;
    }

    public void setPersonArbeidsgiverFnr(String personArbeidsgiverFnr) {
        this.personArbeidsgiverFnr = personArbeidsgiverFnr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsforholdWrapper that = (ArbeidsforholdWrapper) o;
        return Objects.equals(arbeidsgiverIdentifikator, that.arbeidsgiverIdentifikator) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverIdentifikator, arbeidsforholdId);
    }
}
