package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.xml;

import static no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.xml.MottattDokumentXmlParserFeil.FACTORY;
import static no.nav.vedtak.felles.xml.XmlUtils.retrieveNameSpaceOfXML;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.SoeknadsskjemaEngangsstoenadContants;
import no.nav.foreldrepenger.søknad.v1.SøknadConstants;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.seres.xsd.nav.inntektsmelding_m._201809.InntektsmeldingConstants;

public final class MottattDokumentXmlParser {

    private static Map<String, DokumentParserKonfig> SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER = new HashMap<>();

    static {
        SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER.put(SoeknadsskjemaEngangsstoenadContants.NAMESPACE, new DokumentParserKonfig(
            SoeknadsskjemaEngangsstoenadContants.JAXB_CLASS, SoeknadsskjemaEngangsstoenadContants.XSD_LOCATION));
        SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER.put(InntektsmeldingConstants.NAMESPACE, new DokumentParserKonfig(
            InntektsmeldingConstants.JAXB_CLASS, InntektsmeldingConstants.XSD_LOCATION));
        SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER.put(SøknadConstants.NAMESPACE,
            new DokumentParserKonfig(SøknadConstants.JAXB_CLASS, SøknadConstants.XSD_LOCATION,
            SøknadConstants.ADDITIONAL_XSD_LOCATION, SøknadConstants.ADDITIONAL_CLASSES));
    }

    private MottattDokumentXmlParser() {
    }

    @SuppressWarnings("rawtypes")
    public static MottattDokumentWrapper unmarshallXml(String xml) {
        final Object mottattDokument;
        final String namespace = hentNamespace(xml);

        try {
            DokumentParserKonfig dokumentParserKonfig = SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER.get(namespace);
            if (dokumentParserKonfig == null) {
                throw FACTORY.ukjentNamespace(namespace, new IllegalStateException()).toException();
            }
            mottattDokument = JaxbHelper.unmarshalAndValidateXMLWithStAX(dokumentParserKonfig.jaxbClass,
                xml,
                dokumentParserKonfig.xsdLocation,
                dokumentParserKonfig.additionalXsd,
                dokumentParserKonfig.additionalClasses);
            return MottattDokumentWrapper.tilXmlWrapper(mottattDokument);
        } catch (JAXBException | XMLStreamException | SAXException e) {
            throw FACTORY.uventetFeilVedParsingAvSoeknadsXml(namespace, e).toException();
        }
    }

    private static String hentNamespace(String xml) {
        final String namespace;
        try {
            namespace = retrieveNameSpaceOfXML(xml);
        } catch (XMLStreamException e) {
            throw FACTORY.uventetFeilVedParsingAvSoeknadsXml("ukjent", e).toException(); //$NON-NLS-1$
        }
        return namespace;
    }

    private static class DokumentParserKonfig {
        Class<?> jaxbClass;
        String xsdLocation;
        String[] additionalXsd = new String[0];
        Class[] additionalClasses = new Class[0];

        DokumentParserKonfig(Class<?> jaxbClass, String xsdLocation) {
            this.jaxbClass = jaxbClass;
            this.xsdLocation = xsdLocation;
        }

        public DokumentParserKonfig(Class<?> jaxbClass, String xsdLocation, String[] additionalXsd, Class... additionalClasses) {
            this.jaxbClass = jaxbClass;
            this.xsdLocation = xsdLocation;
            this.additionalXsd = additionalXsd;
            this.additionalClasses = additionalClasses;
        }
    }
}
