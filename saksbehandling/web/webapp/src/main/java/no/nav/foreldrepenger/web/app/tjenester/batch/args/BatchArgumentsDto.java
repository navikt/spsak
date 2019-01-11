package no.nav.foreldrepenger.web.app.tjenester.batch.args;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import io.swagger.annotations.ApiParam;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

@JsonAutoDetect(getterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE, fieldVisibility=Visibility.ANY)
public class BatchArgumentsDto implements AbacDto {

    @JsonProperty("jobParameters")
    @ApiParam("Kommaseparert liste med argumenter. eks: id=1,fom=2017-03-03")
    @Pattern(regexp = "[a-zA-Z0-9, =-]*")
    @Size(max = 1000)
    private String jobParameters = "";

    @JsonIgnore
    private Map<String, String> arguments = new HashMap<>();

    public BatchArgumentsDto() {
    }

    private void parseJobParams(Map<String, String> arguments) {
        if ((jobParameters != null) && (jobParameters.length() > 0)) {

            StringTokenizer tokenizer = new StringTokenizer(jobParameters, ",");
            while (tokenizer.hasMoreTokens()) {
                String keyValue = tokenizer.nextToken().trim();
                String[] keyValArr = keyValue.split("=");
                if (keyValArr.length == 2) {
                    arguments.put(keyValArr[0], keyValArr[1]);
                }
            }
        }
    }

    public Map<String, String> getArguments() {
        if (arguments.isEmpty() && hasParameters()) {
            parseJobParams(arguments);
        }
        return arguments;
    }

    private boolean hasParameters() {
        return !jobParameters.isEmpty();
    }

    public void setJobParameters(String jobParameters) {
        this.jobParameters = jobParameters;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); //denne er tom, Batch-API har i praksis rollebasert tilgangskontroll
    }
}
