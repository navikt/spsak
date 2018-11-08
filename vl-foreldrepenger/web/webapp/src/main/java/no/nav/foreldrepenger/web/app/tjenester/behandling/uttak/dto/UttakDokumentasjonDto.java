package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.DokumentasjonPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.UttakDokumentasjonType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class UttakDokumentasjonDto {
    @NotNull
    private LocalDate fom;

    @NotNull
    private LocalDate tom;

    @ValidKodeverk
    private UttakDokumentasjonType dokumentasjonType;

    UttakDokumentasjonDto() { // NOSONAR
        //for jackson
    }

    public UttakDokumentasjonDto(DokumentasjonPeriode dokumentasjon) {
        this.fom = dokumentasjon.getPeriode().getFomDato();
        this.tom = dokumentasjon.getPeriode().getTomDato();
        this.dokumentasjonType = dokumentasjon.getDokumentasjonType();
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public UttakDokumentasjonType getDokumentasjonType() {
        return dokumentasjonType;
    }
}
