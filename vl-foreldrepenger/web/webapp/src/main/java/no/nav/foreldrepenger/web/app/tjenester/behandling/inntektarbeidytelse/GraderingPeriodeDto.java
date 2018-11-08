package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;

public class GraderingPeriodeDto {
    private LocalDate fom;
    private LocalDate tom;
    private BigDecimal arbeidsprosent;

    public GraderingPeriodeDto(Gradering gradering) {
        this.fom = gradering.getPeriode().getFomDato();
        this.tom = gradering.getPeriode().getTomDato();
        this.arbeidsprosent = gradering.getArbeidstidProsent();
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public BigDecimal getArbeidsprosent() {
        return arbeidsprosent;
    }
}
