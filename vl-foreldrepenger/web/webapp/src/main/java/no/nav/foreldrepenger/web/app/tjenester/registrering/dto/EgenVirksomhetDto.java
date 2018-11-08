package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class EgenVirksomhetDto {

    Boolean harArbeidetIEgenVirksomhet;
    
    @Valid
    @Size(max = 10)
    private List<VirksomhetDto> virksomheter;

    public Boolean getHarArbeidetIEgenVirksomhet() {
        return harArbeidetIEgenVirksomhet;
    }

    public void setHarArbeidetIEgenVirksomhet(Boolean harArbeidetIEgenVirksomhet) {
        this.harArbeidetIEgenVirksomhet = harArbeidetIEgenVirksomhet;
    }

    public List<VirksomhetDto> getVirksomheter() {
        return virksomheter;
    }

    public void setVirksomheter(List<VirksomhetDto> virksomheter) {
        this.virksomheter = virksomheter;
    }
}
