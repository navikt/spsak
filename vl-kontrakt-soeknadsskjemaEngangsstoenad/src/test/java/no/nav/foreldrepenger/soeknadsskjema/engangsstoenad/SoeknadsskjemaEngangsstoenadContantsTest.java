package no.nav.foreldrepenger.soeknadsskjema.engangsstoenad;

import static no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.SoeknadsskjemaEngangsstoenadContants.NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

public class SoeknadsskjemaEngangsstoenadContantsTest {

    @Test
    public void skal_finne_og_hente_ut_namespace_fra_xsd() throws Exception {
        final StreamSource streamSource = new StreamSource(getClass().getClassLoader().getResourceAsStream(SoeknadsskjemaEngangsstoenadContants.XSD_LOCATION));
        assertThat(XmlUtils.retrieveNameSpaceOfXSD(streamSource)).isEqualTo(NAMESPACE);
    }

}