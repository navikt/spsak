package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.medlem.api.EndringsresultatPersonopplysningerForMedlemskap;

public class EndringIPersonopplysningDto {
    private boolean erEndret;
    private EndringsresultatPersonopplysningerForMedlemskap.EndretAttributt endretAttributt;
    LocalDate fom;
    LocalDate tom;

    EndringIPersonopplysningDto() { //NOSONAR
    }

    public EndringIPersonopplysningDto(EndringsresultatPersonopplysningerForMedlemskap.Endring endring) {
        erEndret = endring.isErEndret();
        endretAttributt = endring.getEndretAttributt();
        fom = endring.getPeriode().getFomDato();
        tom = endring.getPeriode().getTomDato();
    }

    public boolean isErEndret() {
        return erEndret;
    }

    public void setErEndret(boolean erEndret) {
        this.erEndret = erEndret;
    }

    public EndringsresultatPersonopplysningerForMedlemskap.EndretAttributt getEndretAttributt() {
        return endretAttributt;
    }

    public void setEndretAttributt(EndringsresultatPersonopplysningerForMedlemskap.EndretAttributt endretAttributt) {
        this.endretAttributt = endretAttributt;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }
}
