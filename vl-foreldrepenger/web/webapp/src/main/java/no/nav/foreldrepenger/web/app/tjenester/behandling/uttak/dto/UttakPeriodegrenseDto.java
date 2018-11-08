package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.time.LocalDate;

public class UttakPeriodegrenseDto {

    private LocalDate mottattDato;
    private LocalDate soknadsfristForForsteUttaksdato;
    private LocalDate soknadsperiodeStart;
    private LocalDate soknadsperiodeSlutt;
    private long antallDagerLevertForSent;

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    public LocalDate getSoknadsfristForForsteUttaksdato() {
        return soknadsfristForForsteUttaksdato;
    }

    public void setSoknadsfristForForsteUttaksdato(LocalDate soknadsfristForForsteUttaksdato) {
        this.soknadsfristForForsteUttaksdato = soknadsfristForForsteUttaksdato;
    }

    public LocalDate getSoknadsperiodeStart() {
        return soknadsperiodeStart;
    }

    public void setSoknadsperiodeStart(LocalDate soknadsperiodeStart) {
        this.soknadsperiodeStart = soknadsperiodeStart;
    }

    public LocalDate getSoknadsperiodeSlutt() {
        return soknadsperiodeSlutt;
    }

    public void setSoknadsperiodeSlutt(LocalDate soknadsperiodeSlutt) {
        this.soknadsperiodeSlutt = soknadsperiodeSlutt;
    }

    public long getAntallDagerLevertForSent() {
        return antallDagerLevertForSent;
    }

    public void setAntallDagerLevertForSent(long antallDagerLevertForSent) {
        this.antallDagerLevertForSent = antallDagerLevertForSent;
    }
}
