package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

public class BeregningsgrunnlagAndelDto {
    private String status;
    private String arbeidsgiverNavn;
    private String dagsats;
    private String månedsinntekt;
    private String årsinntekt;
    private String sisteLignedeÅr;
    private String pensjonsgivendeInntekt;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }

    public void setArbeidsgiverNavn(String arbeidsgiverNavn) {
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }

    public String getDagsats() {
        return dagsats;
    }

    public void setDagsats(String dagsats) {
        this.dagsats = dagsats;
    }

    public String getMånedsinntekt() {
        return månedsinntekt;
    }

    public void setMånedsinntekt(String månedsinntekt) {
        this.månedsinntekt = månedsinntekt;
    }

    public String getÅrsinntekt() {
        return årsinntekt;
    }

    public void setÅrsinntekt(String årsinntekt) {
        this.årsinntekt = årsinntekt;
    }

    public String getSisteLignedeÅr() {
        return sisteLignedeÅr;
    }

    public void setSisteLignedeÅr(String sisteLignedeÅr) {
        this.sisteLignedeÅr = sisteLignedeÅr;
    }

    public String getPensjonsgivendeInntekt() {
        return pensjonsgivendeInntekt;
    }

    public void setPensjonsgivendeInntekt(String pensjonsgivendeInntekt) {
        this.pensjonsgivendeInntekt = pensjonsgivendeInntekt;
    }

    @Override
    public String toString() {
        return "BeregningsgrunnlagAndelDto{" +
            "status='" + status + '\'' +
            ", arbeidsgiverNavn='" + arbeidsgiverNavn + '\'' +
            ", dagsats='" + dagsats + '\'' +
            ", månedsinntekt='" + månedsinntekt + '\'' +
            ", årsinntekt='" + årsinntekt + '\'' +
            ", sisteLignedeÅr='" + sisteLignedeÅr + '\'' +
            ", pensjonsgivendeInntekt='" + pensjonsgivendeInntekt + '\'' +
            '}';
    }
}
