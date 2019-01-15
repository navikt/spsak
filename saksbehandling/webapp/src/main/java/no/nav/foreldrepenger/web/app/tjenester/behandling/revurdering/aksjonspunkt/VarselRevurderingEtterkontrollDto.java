package no.nav.foreldrepenger.web.app.tjenester.behandling.revurdering.aksjonspunkt;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;

@JsonTypeName(VarselRevurderingEtterkontrollDto.AKSJONSPUNKT_KODE)
public class VarselRevurderingEtterkontrollDto extends VarselRevurderingDto {
    static final String AKSJONSPUNKT_KODE = "5025";

    VarselRevurderingEtterkontrollDto() {
        // for jackson
    }

    public VarselRevurderingEtterkontrollDto(String begrunnelse, boolean sendVarsel,
            String fritekst, LocalDate frist, Venteårsak ventearsak) {
        super(begrunnelse, sendVarsel, fritekst, frist, ventearsak);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
