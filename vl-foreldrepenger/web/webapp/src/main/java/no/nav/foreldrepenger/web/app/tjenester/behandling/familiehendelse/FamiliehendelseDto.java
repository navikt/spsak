package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.foreldrepenger.web.app.tjenester.behandling.SøknadType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AvklartDataFodselDto.class),
    @JsonSubTypes.Type(value = AvklartDataAdopsjonDto.class),
    @JsonSubTypes.Type(value = AvklartDataOmsorgDto.class)
})
public abstract class FamiliehendelseDto {

    private SøknadType soknadType;
    private LocalDate skjæringstidspunkt;

    public FamiliehendelseDto() {
    }

    public FamiliehendelseDto(SøknadType søknadType) {
        this.soknadType = søknadType;
    }

    public boolean erSoknadsType(SøknadType søknadType) {
        return søknadType.equals(this.soknadType);
    }

    public void setSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        this.skjæringstidspunkt = skjæringstidspunkt;
    }

    @JsonProperty("skjaringstidspunkt")
    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

}
