package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(ManuellRegistreringEndringsøknadDto.AKSJONSPUNKT_KODE)
public class ManuellRegistreringEndringsøknadDto extends ManuellRegistreringDto {

    static final String AKSJONSPUNKT_KODE = "5057";

    @Valid
    private TidsromPermisjonDto tidsromPermisjon;

    public TidsromPermisjonDto getTidsromPermisjon() {
        return tidsromPermisjon;
    }

    public void setTidsromPermisjon(TidsromPermisjonDto tidsromPermisjon) {
        this.tidsromPermisjon = tidsromPermisjon;
    }


    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
