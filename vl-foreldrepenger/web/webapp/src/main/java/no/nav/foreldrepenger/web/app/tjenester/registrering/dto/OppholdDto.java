package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class OppholdDto {

    @NotNull
    @ValidKodeverk
    private OppholdÅrsak årsak;

    @NotNull
    private LocalDate periodeFom;

    @NotNull
    private LocalDate periodeTom;

    public OppholdÅrsak getÅrsak() {
        return årsak;
    }

    public void setÅrsak(OppholdÅrsak årsak) {
        this.årsak = årsak;
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public void setPeriodeFom(LocalDate periodeFom) {
        this.periodeFom = periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    public void setPeriodeTom(LocalDate periodeTom) {
        this.periodeTom = periodeTom;
    }
}
