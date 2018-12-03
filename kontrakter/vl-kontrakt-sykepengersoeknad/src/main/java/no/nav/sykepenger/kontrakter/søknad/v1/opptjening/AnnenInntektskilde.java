package no.nav.sykepenger.kontrakter.søknad.v1.opptjening;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnnenInntektskilde {

    @JsonProperty("type")
    private String type;

    @JsonProperty("erSykemeldt")
    private Boolean erSykemeldt;

    public AnnenInntektskilde(String type, Boolean erSykemeldt) {
        this.type = type;
        this.erSykemeldt = erSykemeldt;
    }

    public AnnenInntektskilde() {
    }

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
