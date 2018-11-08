package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public class AksjonspunktGodkjenningDto {

    private boolean godkjent;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    @Size(min = 4, max = 10)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String aksjonspunktKode;

    @Valid
    @NotNull
    @Size(max = 10)
    private Set<@ValidKodeverk VurderÅrsak> arsaker;

    public AksjonspunktGodkjenningDto() { // NOSONAR
        // For Jackson
    }

    public boolean isGodkjent() {
        return godkjent;
    }

    public void setGodkjent(boolean godkjent) {
        this.godkjent = godkjent;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public Set<VurderÅrsak> getArsaker() {
        return arsaker;
    }

    public void setArsaker(Set<VurderÅrsak> arsaker) {
        this.arsaker = arsaker;
    }

    public void setAksjonspunktKode(String aksjonspunktKode) {
        this.aksjonspunktKode = aksjonspunktKode;
    }

    public String getAksjonspunktKode() {
        return aksjonspunktKode;
    }

    public void setAksjonspunktKode(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        this.aksjonspunktKode = aksjonspunktDefinisjon.getKode();

    }
}
