package no.nav.foreldrepenger.web.app.tjenester.fordeling;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

public class FagsakInfomasjonDto {
    @NotNull
    @Digits(integer = 19, fraction = 0)
    private String aktørId;

    public FagsakInfomasjonDto(String aktørId) {
        this.aktørId = aktørId;
    }

    public FagsakInfomasjonDto() { // For Jackson
    }

    public String getAktørId() {
        return aktørId;
    }
}
