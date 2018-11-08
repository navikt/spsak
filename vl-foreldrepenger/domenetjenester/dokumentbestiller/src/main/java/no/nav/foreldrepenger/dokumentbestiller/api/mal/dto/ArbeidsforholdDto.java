package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import java.util.Objects;

public class ArbeidsforholdDto implements AktivitetDto {

    private String arbeidsgiverNavn;
    private String arbeidsforholdId;
    private long dagsats;
    private boolean gradering;
    private int prosentArbeid;
    private int stillingsprosent;
    private int uttaksgrad;
    private int utbetalingsgrad;

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public int getUttaksgrad() {
        return uttaksgrad;
    }

    public void setUttaksgrad(int uttaksgrad) {
        this.uttaksgrad = uttaksgrad;
    }

    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }

    public void setArbeidsgiverNavn(String arbeidsgiverNavn) {
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }

    public long getDagsats() {
        return dagsats;
    }

    public void setDagsats(long dagsats) {
        this.dagsats = dagsats;
    }

    public boolean getGradering() {
        return gradering;
    }

    public void setGradering(boolean gradering) {
        this.gradering = gradering;
    }

    public int getProsentArbeid() {
        return prosentArbeid;
    }

    public void setProsentArbeid(int prosentArbeid) {
        this.prosentArbeid = prosentArbeid;
    }

    public int getStillingsprosent() {
        return stillingsprosent;
    }

    public void setStillingsprosent(int stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }

    public boolean isGradering() {
        return gradering;
    }

    public int getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public void setUtbetalingsgrad(int utbetalingsgrad) {
        this.utbetalingsgrad = utbetalingsgrad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsforholdDto that = (ArbeidsforholdDto) o;
        return dagsats == that.dagsats &&
            gradering == that.gradering &&
            prosentArbeid == that.prosentArbeid &&
            stillingsprosent == that.stillingsprosent &&
            uttaksgrad == that.uttaksgrad &&
            utbetalingsgrad == that.utbetalingsgrad &&
            Objects.equals(arbeidsgiverNavn, that.arbeidsgiverNavn) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverNavn, arbeidsforholdId, dagsats, gradering, prosentArbeid, stillingsprosent, uttaksgrad, utbetalingsgrad);
    }
}
