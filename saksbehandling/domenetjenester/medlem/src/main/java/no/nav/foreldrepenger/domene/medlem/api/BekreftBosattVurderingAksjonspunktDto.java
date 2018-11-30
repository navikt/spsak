package no.nav.foreldrepenger.domene.medlem.api;

public class BekreftBosattVurderingAksjonspunktDto {
    private Boolean bosattVurdering;

    public BekreftBosattVurderingAksjonspunktDto(Boolean bosattVurdering) {
        this.bosattVurdering = bosattVurdering;
    }

    public Boolean getBosattVurdering() {
        return bosattVurdering;
    }
}
