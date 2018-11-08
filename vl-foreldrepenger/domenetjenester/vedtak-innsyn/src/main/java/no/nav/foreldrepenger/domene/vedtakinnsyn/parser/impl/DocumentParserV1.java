package no.nav.foreldrepenger.domene.vedtakinnsyn.parser.impl;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.domene.vedtakinnsyn.parser.DocumentParser;
import no.nav.foreldrepenger.vedtak.v1.ForeldrepengerVedtakConstants;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class DocumentParserV1 implements DocumentParser {

    @Override
    public void valider(String xml) throws JAXBException, XMLStreamException, SAXException {
        JaxbHelper.unmarshalAndValidateXMLWithStAX(ForeldrepengerVedtakConstants.JAXB_CLASS, xml, ForeldrepengerVedtakConstants.XSD_LOCATION);
    }
}
