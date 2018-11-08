package no.nav.foreldrepenger.domene.medlem.api;

public class BekreftErMedlemVurderingAksjonspunktDto {
    private String manuellVurderingTypeKode;

    public BekreftErMedlemVurderingAksjonspunktDto(String manuellVurderingTypeKode) {
        this.manuellVurderingTypeKode = manuellVurderingTypeKode;
    }

    public String getManuellVurderingTypeKode() {
        return manuellVurderingTypeKode;
    }
}
