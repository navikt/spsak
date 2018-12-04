package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.xml;

import static no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.xml.MottattDokumentXmlParserFeil.FACTORY;
import static no.nav.vedtak.felles.xml.XmlUtils.retrieveNameSpaceOfXML;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.domene.mottak.dokumentmottak.PayloadType;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.json.JacksonJsonConfig;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.foreldrepenger.søknad.v1.SøknadConstants;
import no.nav.sykepenger.kontrakter.søknad.Sykepengersøknad;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.seres.xsd.nav.inntektsmelding_m._201809.InntektsmeldingConstants;

public final class MottattDokumentParser {

    private static Map<String, DokumentParserKonfig> SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER = new HashMap<>();
    private static ObjectMapper objectMapper = JacksonJsonConfig.getObjectMapper();

    static {
        SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER.put(InntektsmeldingConstants.NAMESPACE, new DokumentParserKonfig(
            InntektsmeldingConstants.JAXB_CLASS, InntektsmeldingConstants.XSD_LOCATION));
        SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER.put(SøknadConstants.NAMESPACE,
            new DokumentParserKonfig(SøknadConstants.JAXB_CLASS, SøknadConstants.XSD_LOCATION,
                SøknadConstants.ADDITIONAL_XSD_LOCATION, SøknadConstants.ADDITIONAL_CLASSES));
    }

    private MottattDokumentParser() {
    }

    @SuppressWarnings("rawtypes")
    public static MottattDokumentWrapper unmarshall(PayloadType payloadType, String payload) {

        if (PayloadType.XML.equals(payloadType)) {
            return unmarshallXml(payload);
        }
        if (PayloadType.JSON.equals(payloadType)) {
            return unmarshallJson(payload);
        }
        throw new IllegalArgumentException("Ukjent payload type " + payloadType);
    }

    private static MottattDokumentWrapper<?> unmarshallJson(String payload) {
        try {
            // TODO: Mer generisk støtte for andre varianter at dokumenter på JSON
            Object mottattDokument = objectMapper.readValue(payload, Sykepengersøknad.class);
            return MottattDokumentWrapper.tilWrapper(mottattDokument);
        } catch (IOException e) {
            throw FACTORY.uventetFeilVedParsing(no.nav.sykepenger.kontrakter.søknad.Sykepengersøknad.class.getName(), e).toException();
        }
    }

    private static MottattDokumentWrapper<?> unmarshallXml(String payload) {
        Object mottattDokument;
        final String namespace = hentNamespace(payload);

        try {
            DokumentParserKonfig dokumentParserKonfig = SCHEMA_AND_CLASSES_TIL_STRUKTURERTE_DOKUMENTER.get(namespace);
            if (dokumentParserKonfig == null) {
                throw FACTORY.ukjentNamespace(namespace, new IllegalStateException()).toException();
            }
            mottattDokument = JaxbHelper.unmarshalAndValidateXMLWithStAX(dokumentParserKonfig.jaxbClass,
                payload,
                dokumentParserKonfig.xsdLocation,
                dokumentParserKonfig.additionalXsd,
                dokumentParserKonfig.additionalClasses);
            return MottattDokumentWrapper.tilWrapper(mottattDokument);
        } catch (JAXBException | XMLStreamException | SAXException e) {
            throw FACTORY.uventetFeilVedParsing(namespace, e).toException();
        }
    }

    private static String hentNamespace(String xml) {
        final String namespace;
        try {
            namespace = retrieveNameSpaceOfXML(xml);
        } catch (XMLStreamException e) {
            throw FACTORY.uventetFeilVedParsing("ukjent", e).toException(); //$NON-NLS-1$
        }
        return namespace;
    }

    private static class DokumentParserKonfig {
        Class<?> jaxbClass;
        String xsdLocation;
        String[] additionalXsd = new String[0];
        Class<?>[] additionalClasses = new Class[0];

        DokumentParserKonfig(Class<?> jaxbClass, String xsdLocation) {
            this.jaxbClass = jaxbClass;
            this.xsdLocation = xsdLocation;
        }

        public DokumentParserKonfig(Class<?> jaxbClass, String xsdLocation, String[] additionalXsd, Class<?>... additionalClasses) {
            this.jaxbClass = jaxbClass;
            this.xsdLocation = xsdLocation;
            this.additionalXsd = additionalXsd;
            this.additionalClasses = additionalClasses;
        }
    }
}
