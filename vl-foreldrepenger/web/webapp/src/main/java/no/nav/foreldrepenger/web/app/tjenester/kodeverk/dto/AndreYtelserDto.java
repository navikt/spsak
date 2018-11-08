package no.nav.foreldrepenger.web.app.tjenester.kodeverk.dto;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class AndreYtelserDto {

    @ValidKodeverk
    private ArbeidType ytelseType;
    private LocalDate periodeFom;
    private LocalDate periodeTom;

    public ArbeidType getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(ArbeidType ytelseType) {
        this.ytelseType = ytelseType;
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
