package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem;

import java.time.LocalDate;

public class InntektDto {
    private String navn;
    private String utbetaler;
    private LocalDate fom;
    private LocalDate tom;
    private Boolean ytelse;
    private Integer belop;

    public InntektDto() {
        // trengs for deserialisering av JSON
    }
    public String getNavn() {
        return navn;
    }

    public String getUtbetaler() {
        return utbetaler;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public Boolean getYtelse() {
        return ytelse;
    }

    public Integer getBelop() {
        return belop;
    }

    void setNavn(String navn) {
        this.navn = navn;
    }

    void setUtbetaler(String utbetaler) {
        this.utbetaler = utbetaler;
    }

    void setFom(LocalDate fom) {
        this.fom = fom;
    }

    void setTom(LocalDate tom) {
        this.tom = tom;
    }

    void setYtelse(Boolean ytelse) {
        this.ytelse = ytelse;
    }

    void setBelop(Integer belop) {
        this.belop = belop;
    }
}
