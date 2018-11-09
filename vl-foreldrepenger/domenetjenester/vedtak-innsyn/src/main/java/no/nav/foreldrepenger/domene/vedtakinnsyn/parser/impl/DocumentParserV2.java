package no.nav.foreldrepenger.domene.vedtakinnsyn.parser.impl;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.domene.vedtakinnsyn.parser.DocumentParser;
import no.nav.foreldrepenger.vedtak.v2.VedtakConstants;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class DocumentParserV2 implements DocumentParser {
    @Override
    public void valider(String xml) throws JAXBException, XMLStreamException, SAXException {
        JaxbHelper.unmarshalAndValidateXMLWithStAX(VedtakConstants.JAXB_CLASS, xml, VedtakConstants.XSD_LOCATION, VedtakConstants.ADDITIONAL_XSD_LOCATIONS, VedtakConstants.ADDITIONAL_CLASSES);
    }
}
