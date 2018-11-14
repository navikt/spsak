package no.nav.foreldrepenger.vedtak.v1;

import static no.nav.foreldrepenger.vedtak.v1.ForeldrepengerVedtakConstants.NAMESPACE;
import static no.nav.foreldrepenger.vedtak.v1.ForeldrepengerVedtakConstants.XSD_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

public class ForeldrepengerVedtakConstantsTest {

    @Test
    public void skal_finne_og_hente_ut_namespace_fra_xsd() throws Exception {
        final StreamSource streamSource = new StreamSource(getClass().getClassLoader().getResourceAsStream(XSD_LOCATION));
        assertThat(XmlUtils.retrieveNameSpaceOfXSD(streamSource)).isEqualTo(NAMESPACE);
    }

}