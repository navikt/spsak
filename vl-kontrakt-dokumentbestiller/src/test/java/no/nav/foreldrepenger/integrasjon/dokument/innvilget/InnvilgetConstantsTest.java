package no.nav.foreldrepenger.integrasjon.dokument.innvilget;

import static no.nav.foreldrepenger.integrasjon.dokument.innvilget.InnvilgetConstants.NAMESPACE;
import static no.nav.foreldrepenger.integrasjon.dokument.innvilget.InnvilgetConstants.XSD_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import no.nav.foreldrepenger.integrasjon.dokument.XmlUtils;

public class InnvilgetConstantsTest {
    @Test
    public void skal_finne_og_hente_ut_namespace_fra_xsd() throws Exception {
        final StreamSource streamSource = new StreamSource(getClass().getClassLoader().getResourceAsStream(XSD_LOCATION));
        assertThat(XmlUtils.retrieveNameSpaceOfXSD(streamSource)).isEqualTo(NAMESPACE);
    }
}