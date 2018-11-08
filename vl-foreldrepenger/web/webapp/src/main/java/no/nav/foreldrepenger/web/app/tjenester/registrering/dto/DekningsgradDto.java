package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DekningsgradDto {
    @JsonProperty("100_PROSENT")
    HUNDRE("100"),
    @JsonProperty("80_PROSENT")
    AATI("80");

    private String value;

    DekningsgradDto(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
