package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

@JsonTypeName(KontrollerRevurderingsBehandlingDto.AKSJONSPUNKT_KODE)
public class KontrollerRevurderingsBehandlingDto extends BekreftetAksjonspunktDto {
    static final String AKSJONSPUNKT_KODE = "5055";

    public KontrollerRevurderingsBehandlingDto() {
        //For Jackson
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
