package no.nav.foreldrepenger.domene.medlem.api;

public class BekreftOppholdVurderingAksjonspunktDto {
    private Boolean oppholdsrettVurdering;
    private Boolean lovligOppholdVurdering;
    private Boolean erEosBorger;

    public BekreftOppholdVurderingAksjonspunktDto(Boolean oppholdsrettVurdering, Boolean lovligOppholdVurdering, Boolean erEosBorger) {
        this.oppholdsrettVurdering = oppholdsrettVurdering;
        this.lovligOppholdVurdering = lovligOppholdVurdering;
        this.erEosBorger = erEosBorger;
    }

    public Boolean getOppholdsrettVurdering() {
        return oppholdsrettVurdering;
    }

    public Boolean getLovligOppholdVurdering() {
        return lovligOppholdVurdering;
    }

    public Boolean getErEosBorger() {
        return erEosBorger;
    }
}
