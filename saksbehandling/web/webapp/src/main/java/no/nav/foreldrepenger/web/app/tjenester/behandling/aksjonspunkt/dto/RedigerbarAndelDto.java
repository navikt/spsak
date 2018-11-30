package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RedigerbarAndelDto {


    @NotNull
    @Size(min = 1, max = 200)
    @Pattern(regexp = ".*")
    private String andel;
    @Min(0)
    @Max(Long.MAX_VALUE)
    @Nullable
    private Long andelsnr;
    @Nullable
    private String arbeidsforholdId;
    @NotNull
    private Boolean nyAndel;
    private Boolean lagtTilAvSaksbehandler;


    RedigerbarAndelDto() { // NOSONAR
        // Jackson
    }

    public RedigerbarAndelDto(String andel,
                              Boolean nyAndel,
                              String arbeidsforholdId,
                              Long andelsnr,
                              Boolean lagtTilAvSaksbehandler) {


        this.andel = andel;
        this.nyAndel = nyAndel;
        this.arbeidsforholdId = arbeidsforholdId;
        this.andelsnr = andelsnr;
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }


    public String getAndel() {
        return andel;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public Boolean getNyAndel() {
        return nyAndel;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

}
