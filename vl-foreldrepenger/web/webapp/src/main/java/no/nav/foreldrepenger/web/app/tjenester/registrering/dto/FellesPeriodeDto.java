package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

public class FellesPeriodeDto {

    private LocalDate periodeFom;
    private LocalDate periodeTom;
    
    @NotNull
    @ValidKodeverk
    private MorsAktivitet morsAktivitet;

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

    public MorsAktivitet getMorsAktivitet() {
        return morsAktivitet;
    }

    public void setMorsAktivitet(MorsAktivitet morsAktivitet) {
        this.morsAktivitet = morsAktivitet;
    }
}
