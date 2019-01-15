package no.nav.foreldrepenger.web.app.tjenester.behandling.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class ByttBehandlendeEnhetDto implements AbacDto {
    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Size(min = 1, max = 256)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String enhetNavn;

    @Size(max = 10)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String enhetId;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingVersjon;

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getEnhetId() {
        return enhetId;
    }

    public void setEnhetId(String enhetId) {
        this.enhetId = enhetId;
    }

    public String getEnhetNavn() {
        return enhetNavn;
    }

    public void setEnhetNavn(String enhetNavn) {
        this.enhetNavn = enhetNavn;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public Long getBehandlingVersjon() {
        return behandlingVersjon;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilBehandlingsId(behandlingId);
    }
}
