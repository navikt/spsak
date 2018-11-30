package no.nav.foreldrepenger.web.app.tjenester.saksbehandler.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class FeatureToggleNavnDto {
    @Pattern(regexp = "^[a-zA-ZæøåÆØÅ_\\-0-9.]*$") ///samme som InputValideringRegex.KODEVERK men i tillegg punktum
    @Size(min = 1, max = 100)
    @NotNull
    private String navn;

    public FeatureToggleNavnDto() {
        //trengs for jackson
    }

    public FeatureToggleNavnDto(String navn) {
        this.navn = navn;
    }

    public String getNavn() {
        return navn;
    }

}
