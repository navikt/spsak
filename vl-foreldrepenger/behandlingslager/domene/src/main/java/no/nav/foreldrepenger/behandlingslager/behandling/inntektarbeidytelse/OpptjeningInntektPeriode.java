package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;

public class OpptjeningInntektPeriode {

    private LocalDate fraOgMed;
    private LocalDate tilOgMed;
    private BigDecimal beløp;
    private Opptjeningsnøkkel opptjeningsnøkkel;
    private InntektspostType type;

    public OpptjeningInntektPeriode(Inntektspost inntektspost, Opptjeningsnøkkel opptjeningsnøkkel) {
        this.fraOgMed = inntektspost.getFraOgMed();
        this.tilOgMed = inntektspost.getTilOgMed();
        this.beløp = inntektspost.getBeløp().getVerdi();
        this.opptjeningsnøkkel = opptjeningsnøkkel;
        this.type = inntektspost.getInntektspostType();
    }

    public LocalDate getFraOgMed() {
        return fraOgMed;
    }

    public LocalDate getTilOgMed() {
        return tilOgMed;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public Opptjeningsnøkkel getOpptjeningsnøkkel() {
        return opptjeningsnøkkel;
    }

    public InntektspostType getType() {
        return type;
    }
}
