package no.nav.foreldrepenger.behandling.historikk;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagTotrinnsvurdering;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class HistorikkinnslagTotrinnsVurderingDto {

    @JsonProperty("aksjonspunktBegrunnelse")
    private String aksjonspunktBegrunnelse;
    
    @JsonProperty("godkjent")
    private boolean godkjent;
    
    @JsonProperty("aksjonspunktKode")
    private String aksjonspunktKode;


    public String getBegrunnelse() {
        return aksjonspunktBegrunnelse;
    }

    public void setBegrunnelse(String aksjonspunktBegrunnelse) {
        this.aksjonspunktBegrunnelse = aksjonspunktBegrunnelse;
    }

    public boolean isGodkjent() {
        return godkjent;
    }

    public void setGodkjent(boolean godkjent) {
        this.godkjent = godkjent;
    }

    public String getAksjonspunktKode() {
        return aksjonspunktKode;
    }

    public void setAksjonspunktKode(String aksjonspunktKode) {
        this.aksjonspunktKode = aksjonspunktKode;
    }


    static List<HistorikkinnslagTotrinnsVurderingDto> mapFra(List<HistorikkinnslagTotrinnsvurdering> aksjonspunkter) {
        return aksjonspunkter.stream()
            .map(HistorikkinnslagTotrinnsVurderingDto::mapFra)
            .collect(Collectors.toList());
    }

    private static HistorikkinnslagTotrinnsVurderingDto mapFra(HistorikkinnslagTotrinnsvurdering totrinnsvurdering) {
        HistorikkinnslagTotrinnsVurderingDto dto = new HistorikkinnslagTotrinnsVurderingDto();
        dto.setAksjonspunktKode(totrinnsvurdering.getAksjonspunktDefinisjon().getKode());
        dto.setBegrunnelse(totrinnsvurdering.getBegrunnelse());
        dto.setGodkjent(totrinnsvurdering.erGodkjent());
        return dto;
    }
}
