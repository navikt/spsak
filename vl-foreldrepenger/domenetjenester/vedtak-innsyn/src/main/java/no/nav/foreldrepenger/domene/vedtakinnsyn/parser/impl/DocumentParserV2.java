package no.nav.foreldrepenger.domene.vedtakinnsyn.parser.impl;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import no.nav.foreldrepenger.domene.vedtakinnsyn.parser.DocumentParser;
import no.nav.foreldrepenger.vedtak.v2.VedtakParser;

public class DocumentParserV2 implements DocumentParser {
    @Override
    public void valider(String xml) throws JAXBException, XMLStreamException {
        VedtakParser.unmarshall(xml);
    }
}
