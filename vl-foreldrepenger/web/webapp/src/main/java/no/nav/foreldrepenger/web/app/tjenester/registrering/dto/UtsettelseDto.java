package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class UtsettelseDto {

    @NotNull
    private LocalDate periodeFom;

    @NotNull
    private LocalDate periodeTom;

    @ValidKodeverk
    private UttakPeriodeType periodeForUtsettelse;

    @ValidKodeverk
    private UtsettelseÅrsak arsakForUtsettelse;

    @Pattern(regexp = "[\\d]{9}")
    private String orgNr;

    private boolean erArbeidstaker;


    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    public void setPeriodeFom(LocalDate periodeFom) {
        this.periodeFom = periodeFom;
    }

    public void setPeriodeTom(LocalDate periodeTom) {
        this.periodeTom = periodeTom;
    }

    public UttakPeriodeType getPeriodeForUtsettelse() {
        return periodeForUtsettelse;
    }

    public void setPeriodeForUtsettelse(UttakPeriodeType periodeForUtsettelse) {
        this.periodeForUtsettelse = periodeForUtsettelse;
    }

    public void setArsakForUtsettelse(UtsettelseÅrsak arsakForUtsettelse) {
        this.arsakForUtsettelse = arsakForUtsettelse;
    }

    public UtsettelseÅrsak getArsakForUtsettelse() {
        return arsakForUtsettelse;
    }

    public String getOrgNr() {
        return orgNr;
    }

    public void setOrgNr(String orgNr) {
        this.orgNr = orgNr;
    }

    public boolean isErArbeidstaker() {
        return erArbeidstaker;
    }

    public void setErArbeidstaker(boolean erArbeidstaker) {
        this.erArbeidstaker = erArbeidstaker;
    }
}
