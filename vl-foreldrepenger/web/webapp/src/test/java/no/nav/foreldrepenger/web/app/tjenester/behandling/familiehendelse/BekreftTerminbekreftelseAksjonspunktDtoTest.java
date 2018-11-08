package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.web.app.tjenester.DtoTest;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;

public class BekreftTerminbekreftelseAksjonspunktDtoTest extends DtoTest {

    @Test
    public void test_av_json_mapping() throws IOException {
        BekreftTerminbekreftelseAksjonspunktDto terminbekreftelseAksjonspunktDto = bekreftFødselAksjonspunktDto();
        Writer jsonWriter = new StringWriter();

        objectMapper.writeValue(jsonWriter, terminbekreftelseAksjonspunktDto);

        jsonWriter.flush();
        String json = jsonWriter.toString();

        BekreftTerminbekreftelseAksjonspunktDto objektFraJson = objectMapper.readValue(json, BekreftTerminbekreftelseAksjonspunktDto.class);

        assertThat(objektFraJson.getAntallBarn()).isEqualTo(terminbekreftelseAksjonspunktDto.getAntallBarn());
        assertThat(objektFraJson.getTermindato()).isEqualTo(terminbekreftelseAksjonspunktDto.getTermindato());
        assertThat(objektFraJson.getUtstedtdato()).isEqualTo(terminbekreftelseAksjonspunktDto.getUtstedtdato());
        assertThat(objektFraJson.getBegrunnelse()).isEqualTo(terminbekreftelseAksjonspunktDto.getBegrunnelse());
    }

    private BekreftTerminbekreftelseAksjonspunktDto bekreftFødselAksjonspunktDto() {
        return new BekreftTerminbekreftelseAksjonspunktDto("Test", LocalDate.now().plusDays(30), LocalDate.now().minusDays(10), 1);
    }

}
