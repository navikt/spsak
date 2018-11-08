package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public class BekreftetUttakPeriodeDto {

    @Valid
    private KontrollerFaktaPeriodeDto bekreftetPeriode;

    private LocalDate orginalFom;
    private LocalDate orginalTom;

    @ValidKodeverk
    private UttakPeriodeVurderingType originalResultat = UttakPeriodeVurderingType.PERIODE_IKKE_VURDERT;

    @Min(0)
    //Må ha mer enn 100 her pga at søknad støtter arbeidsprosenter over 100
    @Max(999)
    @Digits(integer = 3, fraction = 2)
    private BigDecimal originalArbeidstidsprosent;
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String originalBegrunnelse;

    public KontrollerFaktaPeriodeDto getBekreftetPeriode() {
        return bekreftetPeriode;
    }

    public void setBekreftetPeriode(KontrollerFaktaPeriodeDto bekreftetPeriode) {
        this.bekreftetPeriode = bekreftetPeriode;
    }

    public UttakPeriodeVurderingType getOriginalResultat() { return originalResultat; }

    public void setOriginalResultat(UttakPeriodeVurderingType originalResultat) { this.originalResultat = originalResultat; }

    public LocalDate getOrginalFom() {
        return orginalFom;
    }

    public void setOrginalFom(LocalDate orginalFom) {
        this.orginalFom = orginalFom;
    }

    public LocalDate getOrginalTom() {
        return orginalTom;
    }

    public void setOrginalTom(LocalDate orginalTom) {
        this.orginalTom = orginalTom;
    }

    public BigDecimal getOriginalArbeidstidsprosent() {
        return originalArbeidstidsprosent;
    }

    public void setOriginalArbeidstidsprosent(BigDecimal originalArbeidstidsprosent) {
        this.originalArbeidstidsprosent = originalArbeidstidsprosent;
    }

    public String getOriginalBegrunnelse() {
        return originalBegrunnelse;
    }

    public void setOriginalBegrunnelse(String originalBegrunnelse) {
        this.originalBegrunnelse = originalBegrunnelse;
    }
}
