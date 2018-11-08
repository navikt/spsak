package no.nav.foreldrepenger.domene.dokument;

import java.time.LocalDate;

public class VarselRevurderingAksjonspunktDto {

    private String fritekst;
    private String begrunnelse;
    private LocalDate frist;
    private String venteÅrsakKode;

    public VarselRevurderingAksjonspunktDto(String fritekst, String begrunnelse, LocalDate frist, String venteÅrsakKode) {
        this.fritekst = fritekst;
        this.begrunnelse = begrunnelse;
        this.frist = frist;
        this.venteÅrsakKode = venteÅrsakKode;
    }

    public String getFritekst() {
        return fritekst;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public LocalDate getFrist() {
        return frist;
    }

    public String getVenteÅrsakKode() {
        return venteÅrsakKode;
    }
}
