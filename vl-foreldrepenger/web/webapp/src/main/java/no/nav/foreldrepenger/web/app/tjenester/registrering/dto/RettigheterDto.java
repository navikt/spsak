package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RettigheterDto {
    @JsonProperty("ANNEN_FORELDER_DOED")
    ANNEN_FORELDER_DOED,
    @JsonProperty("OVERTA_FORELDREANSVARET_ALENE")
    OVERTA_FORELDREANSVARET_ALENE,
    @JsonProperty("MANN_ADOPTERER_ALENE")
    MANN_ADOPTERER_ALENE,
}
