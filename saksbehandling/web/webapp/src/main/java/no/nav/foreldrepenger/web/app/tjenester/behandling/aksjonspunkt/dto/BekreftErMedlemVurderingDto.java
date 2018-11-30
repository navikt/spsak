package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

@JsonTypeName(BekreftErMedlemVurderingDto.AKSJONSPUNKT_KODE)
public class BekreftErMedlemVurderingDto extends BekreftetAksjonspunktDto {

    static final String AKSJONSPUNKT_KODE = "5021";

    @NotNull
    @ValidKodeverk
    private MedlemskapManuellVurderingType manuellVurderingType;

    BekreftErMedlemVurderingDto() { // NOSONAR
        // For Jackson
    }

    public BekreftErMedlemVurderingDto(String begrunnelse, MedlemskapManuellVurderingType manuellVurderingType) { // NOSONAR

        super(begrunnelse);
        this.manuellVurderingType = manuellVurderingType;
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }

    public MedlemskapManuellVurderingType getManuellVurderingType() {
        return manuellVurderingType;
    }

}
