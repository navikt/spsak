package no.nav.foreldrepenger.web.app.tjenester.kodeverk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.vedtak.util.InputValideringRegex;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class NaringsvirksomhetTypeDto {

    @JsonProperty(value = "ANNEN")
    private boolean annen;

    @JsonProperty(value = "FISKE")
    private boolean fiske;

    @JsonProperty(value = "DAGMAMMA")
    private boolean dagmammaEllerFamiliebarnehage;

    @JsonProperty(value = "JORDBRUK_SKOGBRUK")
    private boolean jordbrukEllerSkogbruk;

    @JsonProperty("typeFiske")
    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String typeFiske;

    public boolean getAnnen() {
        return annen;
    }

    public boolean getFiske() {
        return fiske;
    }

    public boolean getJordbrukEllerSkogbruk() {
        return jordbrukEllerSkogbruk;
    }

    public String getTypeFiske() {
        return typeFiske;
    }

    public boolean getDagmammaEllerFamiliebarnehage() {
        return dagmammaEllerFamiliebarnehage;
    }

    public void setAnnen(boolean annen) {
        this.annen = annen;
    }

    public void setFiske(boolean fiske) {
        this.fiske = fiske;
    }

    public void setJordbrukEllerSkogbruk(boolean jordbrukEllerSkogbruk) {
        this.jordbrukEllerSkogbruk = jordbrukEllerSkogbruk;
    }

    public void setTypeFiske(String typeFiske) {
        this.typeFiske = typeFiske;
    }

    public void setDagmammaEllerFamiliebarnehage(boolean dagmammaEllerFamiliebarnehage) {
        this.dagmammaEllerFamiliebarnehage = dagmammaEllerFamiliebarnehage;
    }
}
