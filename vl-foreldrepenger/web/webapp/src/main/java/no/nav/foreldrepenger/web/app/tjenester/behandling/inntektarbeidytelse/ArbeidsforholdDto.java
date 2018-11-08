package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdKilde;

public class ArbeidsforholdDto {

    private String id;
    private String navn;
    // For mottak fra GUI (orgnr for virksomhet, og aktørId for person-arbeidsgiver)
    private String arbeidsgiverIdentifikator;
    // For visning i GUI (orgnr for virksomhet, og fnr for person-arbeidsgiver)
    private String arbeidsgiverIdentifiktorGUI;
    private String arbeidsforholdId;
    private LocalDate fomDato;
    private LocalDate tomDato;
    private ArbeidsforholdKildeDto kilde;
    private LocalDate mottattDatoInntektsmelding;
    private String beskrivelse;
    private BigDecimal stillingsprosent;
    private Boolean brukArbeidsforholdet;
    private Boolean fortsettBehandlingUtenInntektsmelding;
    private Boolean erNyttArbeidsforhold;
    private Boolean erEndret;
    private Boolean erSlettet;
    private String erstatterArbeidsforholdId;
    private Boolean harErstattetEttEllerFlere;
    private Boolean ikkeRegistrertIAaRegister;
    private Boolean tilVurdering;
    private Boolean vurderOmSkalErstattes;

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
        this.fomDato = fomDato;
    }

    public LocalDate getTomDato() {
        return tomDato;
    }

    public void setTomDato(LocalDate tomDato) {
        this.tomDato = tomDato;
    }

    public ArbeidsforholdKildeDto getKilde() {
        return kilde;
    }

    public void setKilde(ArbeidsforholdKilde kilde) {
        this.kilde = new ArbeidsforholdKildeDto(kilde.getNavn());
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

    public String getErstatterArbeidsforholdId() {
        return erstatterArbeidsforholdId;
    }

    public void setErstatterArbeidsforholdId(String erstatterArbeidsforholdId) {
        this.erstatterArbeidsforholdId = erstatterArbeidsforholdId;
    }

    public Boolean getHarErstattetEttEllerFlere() {
        return harErstattetEttEllerFlere;
    }

    public void setHarErstattetEttEllerFlere(Boolean harErstattetEttEllerFlere) {
        this.harErstattetEttEllerFlere = harErstattetEttEllerFlere;
    }

    public Boolean getIkkeRegistrertIAaRegister() {
        return ikkeRegistrertIAaRegister;
    }

    public void setIkkeRegistrertIAaRegister(Boolean ikkeRegistrertIAaRegister) {
        this.ikkeRegistrertIAaRegister = ikkeRegistrertIAaRegister;
    }

    public Boolean getTilVurdering() {
        return tilVurdering;
    }

    public void setTilVurdering(Boolean tilVurdering) {
        this.tilVurdering = tilVurdering;
    }

    public Boolean getVurderOmSkalErstattes() {
        return vurderOmSkalErstattes;
    }

    public void setVurderOmSkalErstattes(boolean vurderOmSkalErstattes) {
        this.vurderOmSkalErstattes = vurderOmSkalErstattes;
    }

    public String getArbeidsgiverIdentifiktorGUI() {
        return arbeidsgiverIdentifiktorGUI;
    }

    public void setArbeidsgiverIdentifiktorGUI(String arbeidsgiverIdentififaktorGUI) {
        this.arbeidsgiverIdentifiktorGUI = arbeidsgiverIdentififaktorGUI;
    }
}
