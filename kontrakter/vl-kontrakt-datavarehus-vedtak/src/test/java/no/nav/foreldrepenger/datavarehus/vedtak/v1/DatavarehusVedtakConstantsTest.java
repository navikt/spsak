package no.nav.foreldrepenger.datavarehus.vedtak.v1;


import org.junit.Test;


import static no.nav.foreldrepenger.datavarehus.vedtak.v1.DatavarehusVedtakConstants.NAMESPACE;
import static no.nav.foreldrepenger.datavarehus.vedtak.v1.DatavarehusVedtakConstants.XSD_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;


import javax.xml.transform.stream.StreamSource;


public class DatavarehusVedtakConstantsTest {
    @Test
    public void skal_finne_og_hente_ut_namespace_fra_xsd() throws Exception {
        final StreamSource streamSource = new StreamSource(getClass().getClassLoader().getResourceAsStream(XSD_LOCATION));
        assertThat(XmlUtils.retrieveNameSpaceOfXSD(streamSource)).isEqualTo(NAMESPACE);
    }

}
