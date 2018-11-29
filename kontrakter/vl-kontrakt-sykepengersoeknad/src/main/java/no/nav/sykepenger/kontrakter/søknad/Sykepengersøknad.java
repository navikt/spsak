package no.nav.sykepenger.kontrakter.søknad;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.sykepenger.kontrakter.søknad.v1.SykepengesøknadV1;

/**
 * Marker class for å støtte flere versjoner..
 */

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "version")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SykepengesøknadV1.class, name = "1.0"),
})
public abstract class Sykepengersøknad {
}
