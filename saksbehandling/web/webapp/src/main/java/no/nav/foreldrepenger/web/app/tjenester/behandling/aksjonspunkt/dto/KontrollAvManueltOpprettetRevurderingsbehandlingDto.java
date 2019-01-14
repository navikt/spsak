package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(KontrollAvManueltOpprettetRevurderingsbehandlingDto.AKSJONSPUNKT_KODE)
public class KontrollAvManueltOpprettetRevurderingsbehandlingDto  extends BekreftetAksjonspunktDto {
    static final String AKSJONSPUNKT_KODE = "5056";

    public KontrollAvManueltOpprettetRevurderingsbehandlingDto() {
        //For Jackson
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}


