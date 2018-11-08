package no.nav.foreldrepenger.domene.familiehendelse;

import java.time.LocalDate;

public class AvklarManglendeFødselAksjonspunktDto {
    private String kode;
    private LocalDate fodselsdato;
    private Integer antallBarn;

    public AvklarManglendeFødselAksjonspunktDto(String kode, LocalDate fodselsdato, Integer antallBarn) {
        this.kode = kode;
        this.fodselsdato = fodselsdato;
        this.antallBarn = antallBarn;
    }

    public String getKode() {
        return kode;
    }

    public LocalDate getFodselsdato() {
        return fodselsdato;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

}
