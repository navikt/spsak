package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.web.app.tjenester.behandling.SøknadType;

public class AvklartDataFodselDto extends FamiliehendelseDto {
    private LocalDate fodselsdato;
    private Integer antallBarnFødt;
    private Boolean brukAntallBarnFraTps;
    private Boolean erOverstyrt;
    private LocalDate termindato;
    private Integer antallBarnTermin;
    private LocalDate utstedtdato;
    private Boolean morForSykVedFodsel;
    private Long vedtaksDatoSomSvangerskapsuke;

    AvklartDataFodselDto() {
        super(SøknadType.FØDSEL);
    }

    void setFodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
    }

    void setAntallBarnFødt(Integer antallBarnFødt) {
        this.antallBarnFødt = antallBarnFødt;
    }

    void setBrukAntallBarnFraTps(Boolean brukAntallBarnFraTps) {
        this.brukAntallBarnFraTps = brukAntallBarnFraTps;
    }

    void setErOverstyrt(Boolean erOverstyrt) {
        this.erOverstyrt = erOverstyrt;
    }

    void setTermindato(LocalDate termindato) {
        this.termindato = termindato;
    }

    public void setMorForSykVedFodsel(Boolean morForSykVedFodsel) {
        this.morForSykVedFodsel = morForSykVedFodsel;
    }

    //TODO(OJR) burde fjerne enten denne eller setAntallBarnFødt

    void setAntallBarnTermin(Integer antallBarnTermin) {
        this.antallBarnTermin = antallBarnTermin;
    }

    void setUtstedtdato(LocalDate utstedtdato) {
        this.utstedtdato = utstedtdato;
    }

    @JsonProperty("fodselsdato")
    public LocalDate getFodselsdato() {
        return fodselsdato;
    }

    @JsonProperty("antallBarnFodsel")
    public Integer getAntallBarnFødt() {
        return antallBarnFødt;
    }

    @JsonProperty("brukAntallBarnFraTps")
    public Boolean getBrukAntallBarnFraTps() {
        return brukAntallBarnFraTps;
    }

    @JsonProperty("termindato")
    public LocalDate getTermindato() {
        return termindato;
    }

    @JsonProperty("antallBarnTermin")
    public Integer getAntallBarnTermin() {
        return antallBarnTermin;
    }

    @JsonProperty("utstedtdato")
    public LocalDate getUtstedtdato() {
        return utstedtdato;
    }

    @JsonProperty("dokumentasjonForeligger")
    public Boolean getErOverstyrt() {
        return erOverstyrt;
    }

    @JsonProperty("morForSykVedFodsel")
    public Boolean getMorForSykVedFodsel() {
        return morForSykVedFodsel;
    }
    
    @JsonProperty("vedtaksDatoSomSvangerskapsuke")
    public Long getVedtaksDatoSomSvangerskapsuke() {
        return vedtaksDatoSomSvangerskapsuke;
    }
    
    void setVedtaksDatoSomSvangerskapsuke(Long vedtaksDatoSomSvangerskapsuke) {
        this.vedtaksDatoSomSvangerskapsuke = vedtaksDatoSomSvangerskapsuke;
    }
}
