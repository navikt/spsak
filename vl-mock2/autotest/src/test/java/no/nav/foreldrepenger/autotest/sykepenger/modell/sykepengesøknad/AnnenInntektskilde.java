package no.nav.foreldrepenger.autotest.sykepenger.modell.sykepengesøknad;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnnenInntektskilde {

    @JsonProperty
    private String type;

    @JsonProperty
    private Boolean erSykemeldt;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getErSykemeldt() {
        return erSykemeldt;
    }

    public void setErSykemeldt(Boolean erSykemeldt) {
        this.erSykemeldt = erSykemeldt;
    }
}
