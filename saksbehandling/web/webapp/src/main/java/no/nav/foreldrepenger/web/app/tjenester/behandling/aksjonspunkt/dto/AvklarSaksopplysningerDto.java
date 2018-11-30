package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(AvklarSaksopplysningerDto.AKSJONSPUNKT_KODE)
public class AvklarSaksopplysningerDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5022";

    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String personstatus;

    private boolean fortsettBehandling;

    AvklarSaksopplysningerDto() {
        //For Jackson
    }


    public AvklarSaksopplysningerDto(String begrunnelse, String personstatus,
                                     boolean fortsettBehandling) {
        super(begrunnelse);
        this.personstatus = personstatus;
        this.fortsettBehandling = fortsettBehandling;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public String getPersonstatus() {
        return personstatus;
    }

    public boolean isFortsettBehandling() {
        return fortsettBehandling;
    }

}
