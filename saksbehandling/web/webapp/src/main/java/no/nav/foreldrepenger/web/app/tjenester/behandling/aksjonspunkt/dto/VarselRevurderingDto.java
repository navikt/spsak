package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public abstract class VarselRevurderingDto extends BekreftetAksjonspunktDto {
    private boolean sendVarsel;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    private LocalDate frist;

    @ValidKodeverk
    private Venteårsak ventearsak;

    public VarselRevurderingDto(String begrunnelse, boolean sendVarsel,
            String fritekst, LocalDate frist, Venteårsak ventearsak) {
        super(begrunnelse);
        this.sendVarsel = sendVarsel;
        this.fritekst = fritekst;
        this.frist = frist;
        this.ventearsak = ventearsak;
    }

    protected VarselRevurderingDto() {
        super();
    }

    public boolean isSendVarsel() {
        return sendVarsel;
    }

    public String getFritekst() {
        return fritekst;
    }

    public LocalDate getFrist() {
        return frist;
    }

    public Venteårsak getVentearsak() {
        return ventearsak;
    }
}
