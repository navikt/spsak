package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class TidsromPermisjonDto {

    @Valid
    private OverføringsperiodeDto overforingsperiode;

    @Valid
    @Size(max = 100)
    private List<PermisjonPeriodeDto> permisjonsPerioder;

    @Valid
    @Size(max = 100)
    private List<GraderingDto> graderingPeriode;

    @Valid
    @Size(max = 100)
    private List<UtsettelseDto> utsettelsePeriode;

    @Valid
    @Size(max = 100)
    private List<OppholdDto> oppholdPerioder;

    private boolean sokerHarAleneomsorg;

    private boolean denAndreForelderenHarRettPaForeldrepenger;

    public OverføringsperiodeDto getOverforingsperiode() {
        return overforingsperiode;
    }

    public void setOverforingsperiode(OverføringsperiodeDto overforingsperiode) {
        this.overforingsperiode = overforingsperiode;
    }

    public List<PermisjonPeriodeDto> getPermisjonsPerioder() {
        return permisjonsPerioder == null ? Collections.emptyList() : permisjonsPerioder;
    }

    public void setPermisjonsPerioder(List<PermisjonPeriodeDto> permisjonsPerioder) {
        this.permisjonsPerioder = permisjonsPerioder;
    }

    public boolean getSokerHarAleneomsorg() {
        return sokerHarAleneomsorg;
    }

    public void setSokerHarAleneomsorg(Boolean sokerHarAleneomsorg) {
        this.sokerHarAleneomsorg = sokerHarAleneomsorg;
    }

    public boolean getDenAndreForelderenHarRettPaForeldrepenger() {
        return denAndreForelderenHarRettPaForeldrepenger;
    }

    public void setDenAndreForelderenHarRettPaForeldrepenger(Boolean denAndreForelderenHarRettPaForeldrepenger) {
        this.denAndreForelderenHarRettPaForeldrepenger = denAndreForelderenHarRettPaForeldrepenger;
    }

    public List<GraderingDto> getGraderingPeriode() {
        return graderingPeriode == null ? Collections.emptyList() : graderingPeriode;
    }

    public void setGraderingPeriode(List<GraderingDto> graderingPeriode) {
        this.graderingPeriode = graderingPeriode;
    }

    public List<UtsettelseDto> getUtsettelsePeriode() {
        return utsettelsePeriode == null ? Collections.emptyList() : utsettelsePeriode;
    }

    public void setUtsettelsePeriode(List<UtsettelseDto> utsettelsePeriode) {
        this.utsettelsePeriode = utsettelsePeriode;
    }

    public List<OppholdDto> getOppholdPerioder() {
        return oppholdPerioder == null ? Collections.emptyList() : oppholdPerioder;
    }

    public void setOppholdPerioder(List<OppholdDto> oppholdPerioder) {
        this.oppholdPerioder = oppholdPerioder;
    }
}
