package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

public class FastsatteVerdierDto {

    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer refusjon;
    @NotNull
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer fastsattBeløp;
    @NotNull
    @ValidKodeverk
    private Inntektskategori inntektskategori;

    FastsatteVerdierDto() { // NOSONAR
        // Jackson
    }

    public FastsatteVerdierDto(Integer refusjon,
                               Integer fastsattBeløp,
                               Inntektskategori inntektskategori) {
        this.refusjon = refusjon;
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
    }


    public Integer getRefusjon() {
        return refusjon;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
