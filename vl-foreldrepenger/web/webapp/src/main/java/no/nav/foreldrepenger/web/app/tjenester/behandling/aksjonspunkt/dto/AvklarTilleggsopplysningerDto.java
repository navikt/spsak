package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(AvklarTilleggsopplysningerDto.AKSJONSPUNKT_KODE)
public class AvklarTilleggsopplysningerDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5009";

    @SuppressWarnings("unused") // NOSONAR
    private AvklarTilleggsopplysningerDto() {
        super();
        //For Jackson
    }

    public AvklarTilleggsopplysningerDto(String begrunnelse) { // NOSONAR
        super(begrunnelse);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

}
