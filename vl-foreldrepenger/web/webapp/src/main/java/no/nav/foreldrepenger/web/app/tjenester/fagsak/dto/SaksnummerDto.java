package no.nav.foreldrepenger.web.app.tjenester.fagsak.dto;

import java.util.Objects;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class SaksnummerDto implements AbacDto {

    @JsonProperty("saksnummer")
    @NotNull
    @Digits(integer = 18, fraction = 0)
    private final String saksnummer;

    public SaksnummerDto(Long saksnummer) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        this.saksnummer = saksnummer.toString();
    }

    public SaksnummerDto(String saksnummer) {
        this.saksnummer = saksnummer;
    }
    
    public SaksnummerDto(Saksnummer saksnummer) {
        this.saksnummer = saksnummer.getVerdi();
    }


    public String getVerdi() {
        return saksnummer;
    }

    public Long getVerdiSomLong() {
        return Long.parseLong(saksnummer);
    }

    @Override
    public String toString() {
        return "SaksnummerDto{" +
            "saksnummer='" + saksnummer + '\'' +
            '}';
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilSaksnummer(getVerdi());
    }
}
