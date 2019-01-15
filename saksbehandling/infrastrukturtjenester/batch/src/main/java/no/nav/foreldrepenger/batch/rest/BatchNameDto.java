package no.nav.foreldrepenger.batch.rest;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiParam;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class BatchNameDto implements AbacDto {

    @ApiParam(value = "Name of batch to run", allowEmptyValue = false, required = true)
    @JsonProperty("batchName")
    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private final String batchName;

    public BatchNameDto(String batchName) {
        this.batchName = batchName;
    }

    public String getVerdi() {
        return batchName;
    }

    @Override
    public String toString() {
        return "BatchNameDto{" +
            "batchName='" + batchName + '\'' +
            '}';
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); // denne er tom, Batch-API har i praksis rollebasert tilgangskontroll
    }

}
