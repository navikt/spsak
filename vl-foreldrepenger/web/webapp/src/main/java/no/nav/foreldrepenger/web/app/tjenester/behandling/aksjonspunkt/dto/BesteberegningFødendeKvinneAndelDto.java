package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class BesteberegningFødendeKvinneAndelDto {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long andelsnr;

    @NotNull
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer inntektPrMnd;

    @NotNull
    @ValidKodeverk
    private Inntektskategori inntektskategori;

    BesteberegningFødendeKvinneAndelDto() {
        // For Jackson
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public Integer getInntektPrMnd() {
        return inntektPrMnd;
    }

    public void setInntektPrMnd(Integer inntektPrMnd) {
        this.inntektPrMnd = inntektPrMnd;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }
}
