package no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger;

import static no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.InnhentopplysningerConstants.NAMESPACE;
import static no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.InnhentopplysningerConstants.XSD_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import no.nav.foreldrepenger.integrasjon.dokument.XmlUtils;

public class InnhentopplysningerConstantsTest {

    @Test
    public void skal_finne_og_hente_ut_namespace_fra_xsd() throws Exception {
        final StreamSource streamSource = new StreamSource(getClass().getClassLoader().getResourceAsStream(XSD_LOCATION));
        assertThat(XmlUtils.retrieveNameSpaceOfXSD(streamSource)).isEqualTo(NAMESPACE);
    }

}