package no.nav.foreldrepenger.domene.vedtak.innsyn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.domene.vedtak.innsyn.VedtakXMLTilHTMLTransformator;
import no.nav.vedtak.exception.TekniskException;

public class VedtakXMLTilHTMLTransformatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void skal_få_exception_ved_transformasjon_av_helt_feil_XML() {
        expectedException.expect(TekniskException.class);

        VedtakXMLTilHTMLTransformator.transformer("tull", 1L);
    }

    @Test
    public void skal_transformere_XML_til_HTML_v2() throws Exception {
        les("/eksempel-vedtakHTML.html");
        String inputXML = les("/eksempel_vedtakXML_v2.xml");
        VedtakXMLTilHTMLTransformator.transformer(inputXML, 1L);

    }

    @Test
    public void skal_transformere_XML_til_HTML() throws Exception {
        String forventet = les("/eksempel-vedtakHTML.html");
        String inputXML = les("/eksempel-vedtakXML.xml");
        String resultat = VedtakXMLTilHTMLTransformator.transformer(inputXML, 1L);

        Assertions.assertThat(cleanWhitespace(resultat)).isEqualTo(cleanWhitespace(forventet));
    }

    private String cleanWhitespace(String str) {
        return str.replaceAll("\\r\\n", "") // sammenligner ikke eol
                .replaceAll("\\s", "") // sammenligner ikke heller annen whitespace (diff i genrering mellom Java8/10)
                ;
    }

    private String les(String filnavn) throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(filnavn);
                Scanner scanner = new Scanner(resource, "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }

    }

}
