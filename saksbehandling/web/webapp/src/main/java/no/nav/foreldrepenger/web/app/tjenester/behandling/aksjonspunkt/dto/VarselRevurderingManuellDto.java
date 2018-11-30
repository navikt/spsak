package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;

@JsonTypeName(VarselRevurderingManuellDto.AKSJONSPUNKT_KODE)
public class VarselRevurderingManuellDto extends VarselRevurderingDto {
    static final String AKSJONSPUNKT_KODE = "5026";

    VarselRevurderingManuellDto() {
        // for jackson 
    }

    public VarselRevurderingManuellDto(String begrunnelse, boolean sendVarsel,
            String fritekst, LocalDate frist, Venteårsak ventearsak) {
        super(begrunnelse, sendVarsel, fritekst, frist, ventearsak);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
