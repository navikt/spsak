package no.nav.foreldrepenger.domene.familiehendelse;

import java.time.LocalDate;

public class TerminbekreftelseAksjonspunktDto {

    private LocalDate termindato;
    private LocalDate utstedtdato;
    private Integer antallBarn;
    private String kode;

    public TerminbekreftelseAksjonspunktDto(LocalDate termindato, LocalDate utstedtdato, Integer antallBarn, String kode) {
        this.termindato = termindato;
        this.utstedtdato = utstedtdato;
        this.antallBarn = antallBarn;
        this.kode = kode;
    }

    public LocalDate getTermindato() {
        return termindato;
    }

    public LocalDate getUtstedtdato() {
        return utstedtdato;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public String getKode() {
        return kode;
    }
}
