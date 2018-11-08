package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.LocalDate;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.SoeknadsskjemaEngangsstoenadContants;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.ObjectFactory;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.søknad.v1.SøknadConstants;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

public class IntegrasjonstestUtils {
    public static Long finnAksjonspunkt(Set<Aksjonspunkt> aksjonspunkter, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return aksjonspunkter.stream().filter(ap -> ap.getAksjonspunktDefinisjon().equals(aksjonspunktDefinisjon))
            .map(Aksjonspunkt::getId)
            .findFirst().orElseThrow((() -> new IllegalArgumentException("Fant ikke aksjonspunkt type:" + aksjonspunktDefinisjon)));
    }

    static String lagSøknadXml(SoeknadsskjemaEngangsstoenad søknad) {
        String søknadXml;
        try {
            søknadXml = JaxbHelper.marshalAndValidateJaxb(SoeknadsskjemaEngangsstoenadContants.JAXB_CLASS,
                new ObjectFactory().createSoeknadsskjemaEngangsstoenad(søknad),
                SoeknadsskjemaEngangsstoenadContants.XSD_LOCATION);
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException("Ugyldig marshalling (skal ikke kunne havne her.)", e);
        }
        return søknadXml;
    }

    public static String lagSøknadXml(Soeknad søknad) {
        String søknadXml = "";
        try {
                søknadXml = JaxbHelper.marshalAndValidateJaxb(SøknadConstants.JAXB_CLASS,
                    new no.nav.vedtak.felles.xml.soeknad.v1.ObjectFactory().createSoeknad(søknad),
                    SøknadConstants.XSD_LOCATION,
                    SøknadConstants.ADDITIONAL_XSD_LOCATION,
                    SøknadConstants.ADDITIONAL_CLASSES);
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException("Ugyldig marshalling (skal ikke kunne havne her.)", e);
        }
        return søknadXml;
    }

    public static LocalDate hentFødselsdatoFraFnr(String fnr) {
        return LocalDate.parse(fnr.substring(0, 6), ofPattern("ddMMyy"));
    }
}
