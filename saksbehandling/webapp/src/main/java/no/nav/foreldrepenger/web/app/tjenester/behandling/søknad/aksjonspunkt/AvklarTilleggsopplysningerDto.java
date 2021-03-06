package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad.aksjonspunkt;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;

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
