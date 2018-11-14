package no.nav.foreldrepenger.kontrakter.fordel;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JournalpostMottakDtoTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void skal_kunne_hente_ut_payload_når_lengde_er_definiert_riktig() throws Exception {
        JournalpostMottakDto dto = new JournalpostMottakDto(null, null, null, null, null, "foo");

        assertThat(dto.getPayloadXml()).isPresent();
        assertThat(dto.getPayloadXml().get()).isEqualTo("foo");

        String foobar = base64("<foo>ååå<foo/>");
        Optional<String> validert = JournalpostMottakDto.getPayloadValiderLengde(foobar, 14);

        assertThat(validert.isPresent()).isTrue();
        assertThat(validert.get()).isEqualTo("<foo>ååå<foo/>");
    }

    @Test
    public void skal_feile_når_payload_lenged_ikke_stemmer() throws Exception {
        expectedException.expectMessage("Avsender oppgav at lengde på innhold var 4, men lengden var egentlig 6");

        String foobar = base64("foobar");
        JournalpostMottakDto.getPayloadValiderLengde(foobar, 4);
    }

    @Test
    public void skal_feile_når_payload_lengde_ikke_finnes() throws Exception {
        expectedException.expectMessage("Avsender sendte payload, men oppgav ikke lengde på innhold");

        String foobar = base64("foobar");
        JournalpostMottakDto.getPayloadValiderLengde(foobar, null);
    }

    @Test
    public void skal_være_ok_å_ikke_ha_payload_lengt_når_det_ikke_er_med_payload() throws Exception {
        JournalpostMottakDto dto = new JournalpostMottakDto(null, null, null, null, null, null);
        assertThat(dto.getPayloadXml()).isNotPresent();

        Optional<String> resultat = JournalpostMottakDto.getPayloadValiderLengde(null, null);
        assertThat(resultat).isNotPresent();
    }

    private String base64(String unencoded) {
        byte[] bytes = unencoded.getBytes(Charset.forName("UTF-8"));
        return Base64.getUrlEncoder().encodeToString(bytes);
    }
}