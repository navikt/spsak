package no.nav.foreldrepenger.domene.vedtakinnsyn.parser;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

public interface DocumentParser {

    void valider(String xml) throws JAXBException, XMLStreamException, SAXException;

}
