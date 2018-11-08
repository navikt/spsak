package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;

public class EndringBeregningsgrunnlagAndelDto extends FaktaOmBeregningAndelDto {

    private static final int MÅNEDER_I_1_ÅR = 12;
    private BigDecimal fordelingForrigeBehandling;
    private BigDecimal fordelingForrigeBehandlingPrAar;
    private BigDecimal refusjonskrav = BigDecimal.ZERO;
    private BigDecimal beregnetPrMnd;
    private BigDecimal beregnetPrAar;
    private BigDecimal belopFraInntektsmelding;
    private BigDecimal fastsattForrige;
    private BigDecimal fastsattForrigePrAar;
    private BigDecimal refusjonskravFraInntektsmelding;

    public BigDecimal getBelopFraInntektsmelding() {
        return belopFraInntektsmelding;
    }

    public void setBelopFraInntektsmelding(BigDecimal belopFraInntektsmelding) {
        this.belopFraInntektsmelding = belopFraInntektsmelding;
    }

    public BigDecimal getFordelingForrigeBehandling() {
        return fordelingForrigeBehandling;
    }

    public void setFordelingForrigeBehandling(BigDecimal fordelingForrigeBehandling) {
        this.fordelingForrigeBehandling = fordelingForrigeBehandling;
    }

    public BigDecimal getRefusjonskrav() {
        return refusjonskrav;
    }

    public void setRefusjonskrav(BigDecimal refusjonskrav) {
        this.refusjonskrav = refusjonskrav;
    }

    public void setInntektskategori(Optional<BeregningsgrunnlagPrStatusOgAndel> andelIGjeldendeGrunnlag, BeregningsgrunnlagPrStatusOgAndel nyAndel) {
        if (andelIGjeldendeGrunnlag.isPresent()) {
            setInntektskategori(andelIGjeldendeGrunnlag.get().getInntektskategori() != null ?
                andelIGjeldendeGrunnlag.get().getInntektskategori() : nyAndel.getInntektskategori());
        } else {
            setInntektskategori(nyAndel.getInntektskategori());
        }
    }

    public void initialiserVerdierForBeregnet(BigDecimal beregnetPrAar) {
        this.beregnetPrAar = beregnetPrAar;
        this.beregnetPrMnd = beregnetPrAar != null ? beregnetPrAar.divide(BigDecimal.valueOf(MÅNEDER_I_1_ÅR), 0, RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getBeregnetPrMnd() {
        return beregnetPrMnd;
    }

    public BigDecimal getFastsattForrige() {
        return fastsattForrige;
    }

    public void utledVerdierForFastsattForrige(Optional<BeregningsgrunnlagPrStatusOgAndel> andelIGjeldendeGrunnlag) {
        andelIGjeldendeGrunnlag
            .ifPresent(andel -> {
                if (andel.getBeregnetPrÅr() != null && Boolean.TRUE.equals(andel.getFastsattAvSaksbehandler())) {
                    this.fastsattForrige = andel.getBeregnetPrÅr().divide(BigDecimal.valueOf(MÅNEDER_I_1_ÅR), 0, BigDecimal.ROUND_HALF_UP);
                    this.fastsattForrigePrAar = andel.getBeregnetPrÅr();
                } else {
                    this.fastsattForrige = null;
                    this.fastsattForrigePrAar = null;
                }
            });
    }

    public BigDecimal getRefusjonskravFraInntektsmelding() {
        return refusjonskravFraInntektsmelding;
    }

    public void setRefusjonskravFraInntektsmelding(BigDecimal refusjonskravFraInntektsmelding) {
        this.refusjonskravFraInntektsmelding = refusjonskravFraInntektsmelding;
    }

    public BigDecimal getFordelingForrigeBehandlingPrAar() {
        return fordelingForrigeBehandlingPrAar;
    }

    public void setFordelingForrigeBehandlingPrAar(BigDecimal fordelingForrigeBehandlingPrAar) {
        this.fordelingForrigeBehandlingPrAar = fordelingForrigeBehandlingPrAar;
    }

    public BigDecimal getBeregnetPrAar() {
        return beregnetPrAar;
    }

    public void setBeregnetPrAar(BigDecimal beregnetPrAar) {
        this.beregnetPrAar = beregnetPrAar;
    }

    public BigDecimal getFastsattForrigePrAar() {
        return fastsattForrigePrAar;
    }

    public void setBeregnetPrMnd(BigDecimal beregnetPrMnd) {
        this.beregnetPrMnd = beregnetPrMnd;
    }

    public void setFastsattForrige(BigDecimal fastsattForrige) {
        this.fastsattForrige = fastsattForrige;
    }

    public void setFastsattForrigePrAar(BigDecimal fastsattForrigePrAar) {
        this.fastsattForrigePrAar = fastsattForrigePrAar;
    }
}
