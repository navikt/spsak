package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.BekreftetAksjonspunktDto;

public abstract class BekreftOppholdVurderingDto extends BekreftetAksjonspunktDto {

    private Boolean oppholdsrettVurdering;
    private Boolean lovligOppholdVurdering;
    private Boolean erEosBorger;

    BekreftOppholdVurderingDto() {
        // For Jackson
    }

    public BekreftOppholdVurderingDto(String begrunnelse, Boolean oppholdsrettVurdering,
            Boolean lovligOppholdVurdering, Boolean erEosBorger) { // NOSONAR

        super(begrunnelse);
        this.oppholdsrettVurdering = oppholdsrettVurdering;
        this.lovligOppholdVurdering = lovligOppholdVurdering;
        this.erEosBorger = erEosBorger;
    }


    public Boolean getOppholdsrettVurdering() {
        return oppholdsrettVurdering;
    }

    public Boolean getErEosBorger() {
        return erEosBorger;
    }

    public Boolean getLovligOppholdVurdering() {
        return lovligOppholdVurdering;
    }
    
    @JsonTypeName(BekreftLovligOppholdVurderingDto.AKSJONSPUNKT_KODE)
    public static class BekreftLovligOppholdVurderingDto extends BekreftOppholdVurderingDto {

        static final String AKSJONSPUNKT_KODE = "5019";
        
        @SuppressWarnings("unused") // NOSONAR
        private BekreftLovligOppholdVurderingDto() {
            // For Jackson
        }

        public BekreftLovligOppholdVurderingDto(String begrunnelse, Boolean oppholdsrettVurdering,
                Boolean lovligOppholdVurdering, Boolean erEosBorger) { // NOSONAR
            super(begrunnelse, oppholdsrettVurdering, lovligOppholdVurdering, erEosBorger);
        }

        
        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }
    
    @JsonTypeName(BekreftOppholdsrettVurderingDto.AKSJONSPUNKT_KODE)
    public static class BekreftOppholdsrettVurderingDto extends BekreftOppholdVurderingDto {

        static final String AKSJONSPUNKT_KODE = "5023";
        
        @SuppressWarnings("unused") // NOSONAR
        private BekreftOppholdsrettVurderingDto() {
            // For Jackson
        }

        public BekreftOppholdsrettVurderingDto(String begrunnelse, Boolean oppholdsrettVurdering,
                Boolean lovligOppholdVurdering, Boolean erEosBorger) { // NOSONAR
            super(begrunnelse, oppholdsrettVurdering, lovligOppholdVurdering, erEosBorger);
        }

        
        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

}
