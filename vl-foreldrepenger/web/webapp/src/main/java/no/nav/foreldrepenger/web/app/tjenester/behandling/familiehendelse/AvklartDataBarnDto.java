package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvklartDataBarnDto {

    @JsonProperty("aktorId")
    @Valid
    private AktørId aktørId;

    @JsonProperty("nummer")
    @NotNull
    @Min(0)
    @Max(9)
    private Integer nummer;

    @JsonProperty("fodselsdato")
    @NotNull
    private LocalDate fødselsdato;

    @JsonProperty("opplysningsKilde")
    @ValidKodeverk
    private OpplysningsKilde opplysningsKilde;

    public AktørId getAktørId() {
        return aktørId;
    }

    public void setAktørId(AktørId aktorId) {
        this.aktørId = aktorId;
    }

    public Integer getNummer() {
        return nummer;
    }

    public void setNummer(Integer nummer) {
        this.nummer = nummer;
    }

    public LocalDate getFodselsdato() {
        return fødselsdato;
    }

    public void setFodselsdato(LocalDate fodselsdato) {
        this.fødselsdato = fodselsdato;
    }

    public OpplysningsKilde getOpplysningsKilde() {
        return opplysningsKilde;
    }

    public void setOpplysningsKilde(OpplysningsKilde opplysningsKilde) {
        this.opplysningsKilde = opplysningsKilde;
    }
}
