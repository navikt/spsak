package no.nav.foreldrepenger.søknad.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

public class XmlUtils {
    private static final String TARGET_NAMESPACE = "targetNamespace";

    public static String retrieveNameSpaceOfXSD(Source xsdSource) throws XMLStreamException {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlif.createXMLStreamReader(xsdSource);
        while (!xmlStreamReader.isStartElement()) {
            xmlStreamReader.next();
        }
        return xmlStreamReader.getAttributeValue(null, TARGET_NAMESPACE);
    }
}
