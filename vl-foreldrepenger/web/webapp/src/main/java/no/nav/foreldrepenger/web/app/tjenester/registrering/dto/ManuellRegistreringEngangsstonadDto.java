package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(ManuellRegistreringEngangsstonadDto.AKSJONSPUNKT_KODE)
public class ManuellRegistreringEngangsstonadDto extends ManuellRegistreringDto {
    static final String AKSJONSPUNKT_KODE = "5012";

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
