package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import java.time.LocalDate;
import java.util.Optional;

public class DokumentBeregningsresultatDto {
    //Innvilgelse
    private Integer antallArbeidsgivere;
    //Avslag og opphør
    private LocalDate førsteStønadsDato;
    private LocalDate sisteStønadsDato;
    private LocalDate opphorDato;

    public Integer getAntallArbeidsgivere() {
        return antallArbeidsgivere;
    }

    public void setAntallArbeidsgivere(Integer antallArbeidsgivere) {
        this.antallArbeidsgivere = antallArbeidsgivere;
    }

    public Optional<LocalDate> getFørsteStønadsDato() {
        return Optional.ofNullable(førsteStønadsDato);
    }

    public void setFørsteStønadsDato(LocalDate førsteStønadsDato) {
        this.førsteStønadsDato = førsteStønadsDato;
    }

    public Optional<LocalDate> getSisteStønadsDato() {
        return Optional.ofNullable(sisteStønadsDato);
    }

    public void setSisteStønadsDato(LocalDate sisteStønadsDato) {
        this.sisteStønadsDato = sisteStønadsDato;
    }

    public Optional<LocalDate> getOpphorDato() {
        return Optional.ofNullable(opphorDato);
    }

    public void setOpphorDato(LocalDate opphorDato) {
        this.opphorDato = opphorDato;
    }
}
