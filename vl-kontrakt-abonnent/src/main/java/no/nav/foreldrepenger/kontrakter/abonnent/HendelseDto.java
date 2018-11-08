package no.nav.foreldrepenger.kontrakter.abonnent;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.foreldrepenger.kontrakter.abonnent.infotrygd.InfotrygdHendelseDto;
import no.nav.foreldrepenger.kontrakter.abonnent.tps.FødselHendelseDto;
import no.nav.vedtak.util.InputValideringRegex;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FødselHendelseDto.class, name = FødselHendelseDto.AVSENDER),
        @JsonSubTypes.Type(value = InfotrygdHendelseDto.class, name = InfotrygdHendelseDto.AVSENDER)
})
public abstract class HendelseDto {

    @NotNull
    @Pattern(regexp = "^[a-zA-ZæøåÆØÅ_\\-0-9]*")
    @Size(min = 1, max = 100)
    private String id; // unik per hendelse

    public HendelseDto() {
        // Jackson
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract String getAvsenderSystem();

    public abstract String getHendelsetype();

}
