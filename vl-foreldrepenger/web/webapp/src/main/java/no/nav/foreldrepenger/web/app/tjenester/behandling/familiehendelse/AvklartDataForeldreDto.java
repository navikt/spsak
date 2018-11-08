package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvklartDataForeldreDto {

    @JsonProperty("aktorId")
    @Valid
    @NotNull
    private AktørId aktørId;

    @JsonProperty("dodsdato")
    private LocalDate dødsdato;

    @JsonProperty("opplysningsKilde")
    @ValidKodeverk
    private OpplysningsKilde opplysningsKilde;

    public AktørId getAktorId() {
        return aktørId;
    }

    public void setAktorId(AktørId aktorId) {
        this.aktørId = aktorId;
    }

    public LocalDate getDødsdato() {
        return dødsdato;
    }

    public void setDødsdato(LocalDate dødsdato) {
        this.dødsdato = dødsdato;
    }

    public OpplysningsKilde getOpplysningsKilde() {
        return opplysningsKilde;
    }

    public void setOpplysningsKilde(OpplysningsKilde opplysningsKilde) {
        this.opplysningsKilde = opplysningsKilde;
    }

    public void settOpplysningsKilde(OpplysningsKilde opplysningsKilde) {
        setOpplysningsKilde(opplysningsKilde);
    }
}
