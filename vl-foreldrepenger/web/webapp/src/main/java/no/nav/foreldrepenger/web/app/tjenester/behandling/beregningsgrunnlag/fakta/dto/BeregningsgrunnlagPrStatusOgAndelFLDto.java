package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto;

import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto.BeregningsgrunnlagPrStatusOgAndelDto;

public class BeregningsgrunnlagPrStatusOgAndelFLDto extends BeregningsgrunnlagPrStatusOgAndelDto {
    private Boolean erNyoppstartetEllerSammeOrganisasjon;

    public BeregningsgrunnlagPrStatusOgAndelFLDto() {
        super();
        // trengs for deserialisering av JSON
    }

    public Boolean getErNyoppstartetEllerSammeOrganisasjon() {
        return erNyoppstartetEllerSammeOrganisasjon;
    }

    public void setErNyoppstartetEllerSammeOrganisasjon(Boolean erNyoppstartetEllerSammeOrganisasjon) {
        this.erNyoppstartetEllerSammeOrganisasjon = erNyoppstartetEllerSammeOrganisasjon;
    }
}
