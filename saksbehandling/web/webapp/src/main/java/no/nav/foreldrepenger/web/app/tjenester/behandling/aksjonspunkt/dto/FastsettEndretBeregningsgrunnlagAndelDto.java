package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FastsettEndretBeregningsgrunnlagAndelDto extends RedigerbarAndelDto {

    @Valid
    @NotNull
    private FastsatteVerdierDto fastsatteVerdier;

    FastsettEndretBeregningsgrunnlagAndelDto() { // NOSONAR
        // Jackson
    }

    public FastsettEndretBeregningsgrunnlagAndelDto(RedigerbarAndelDto andelDto,
                                                    FastsatteVerdierDto fastsatteVerdier) {

        super(andelDto.getAndel(), andelDto.getNyAndel(), andelDto.getArbeidsforholdId(),
            andelDto.getAndelsnr(), andelDto.getLagtTilAvSaksbehandler());
        this.fastsatteVerdier = fastsatteVerdier;
    }

    public FastsatteVerdierDto getFastsatteVerdier() {
        return fastsatteVerdier;
    }
}
