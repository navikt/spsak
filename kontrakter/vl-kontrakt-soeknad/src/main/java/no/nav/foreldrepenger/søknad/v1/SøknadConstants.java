package no.nav.foreldrepenger.søknad.v1;

import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.ObjectFactory;

public final class SøknadConstants {
    public static final String NAMESPACE = "urn:no:nav:vedtak:felles:xml:soeknad:v1";
    public static final String YTELSE_IDENTIFIER = "omYtelse";
    public static final String XSD_LOCATION = "xsd/soeknad.xsd";
    public static final String[] ADDITIONAL_XSD_LOCATION = new String[]{
            "xsd/foreldrepenger/foreldrepenger.xsd",
            "xsd/endringssoeknad/endringssoeknad.xsd",
            "xsd/engangsstoenad/engangsstoenad.xsd"};
    public static final Class<?> JAXB_CLASS = no.nav.vedtak.felles.xml.soeknad.v1.Soeknad.class;
    public static final Class<?>[] ADDITIONAL_CLASSES = {Foreldrepenger.class, Engangsstønad.class, Endringssoeknad.class, ObjectFactory.class, no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.ObjectFactory.class, no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.ObjectFactory.class};

    private SøknadConstants() {
    }

}
