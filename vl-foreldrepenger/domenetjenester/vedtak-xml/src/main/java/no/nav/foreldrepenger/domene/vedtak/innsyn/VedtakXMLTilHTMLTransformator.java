package no.nav.foreldrepenger.domene.vedtak.innsyn;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.domene.vedtak.innsyn.parser.DocumentParser;
import no.nav.vedtak.felles.xml.XmlUtils;

public class VedtakXMLTilHTMLTransformator {

    private VedtakXMLTilHTMLTransformator() {
    }

    public static String transformer(String vedtakXML, Long lagretVedtakId) {

        String namespace = getNameSpace(lagretVedtakId, vedtakXML);
        validerXml(namespace, lagretVedtakId, vedtakXML);

        try {
            String xslTransformerFilename = HtmlTransformerProvider.get(namespace);
            Transformer transformer = lagTransformer(xslTransformerFilename);
            Source vedtakXmlStream = lagInputStream(vedtakXML, lagretVedtakId);
            return transformer(transformer, vedtakXmlStream);
        } catch (TransformerConfigurationException e) {
            throw TransformerVedtakXmlFeil.FACTORY.feilVedTransformeringAvVedtakXml(lagretVedtakId, e).toException();
        } catch (TransformerException e) {
            Integer lineNumber = e.getLocator() != null ? e.getLocator().getLineNumber() : null;
            Integer columnNumber = e.getLocator() != null ? e.getLocator().getColumnNumber() : null;
            throw TransformerVedtakXmlFeil.FACTORY.feilVedTransformeringAvVedtakXml(lagretVedtakId, lineNumber, columnNumber, e).toException();
        }
    }

    private static String getNameSpace(Long lagretVedtakId, String xml) {
        String nameSpaceOfXML = "ukjent";
        try {
            return  XmlUtils.retrieveNameSpaceOfXML(xml);
        } catch (XMLStreamException e) {
            throw TransformerVedtakXmlFeil.FACTORY.vedtakXmlValiderteIkke(lagretVedtakId, nameSpaceOfXML, e).toException();
        }
    }

    private static void validerXml(String namespace, Long lagretVedtakId, String xml) {
        try {
            DocumentParser documentParser = DocumentParserProvider.get(lagretVedtakId, namespace);
            documentParser.valider(xml);
        } catch (XMLStreamException | JAXBException | SAXException e) {
            throw TransformerVedtakXmlFeil.FACTORY.vedtakXmlValiderteIkke(lagretVedtakId, namespace, e).toException();
        }
    }

    private static String transformer(Transformer transformer, Source vedtakXmlStream) throws TransformerException {
        StringWriter resultat = new StringWriter();
        StreamResult streamResultat = new StreamResult(resultat);

        transformer.transform(vedtakXmlStream, streamResultat);

        return resultat.toString();
    }

    @SuppressWarnings("resource")
    private static Transformer lagTransformer(String xslTransformerFilename) throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // FIXME (ToreEndestad): Denne kan caches statisk og gjenbrukes (template)
        InputStream inputStream = classLoader.getResourceAsStream(xslTransformerFilename);
        Templates template = factory.newTemplates(new StreamSource(inputStream));

        return template.newTransformer();
    }

    private static Source lagInputStream(String vedtakXML, Long lagretVedtakId) {
        InputStream sourceStream = null;
        try {
            sourceStream = new ByteArrayInputStream(vedtakXML.getBytes(StandardCharsets.UTF_8.name()));
            return new StreamSource(sourceStream);
        } catch (UnsupportedEncodingException e) {
            throw TransformerVedtakXmlFeil.FACTORY.ioFeilVedTransformeringAvVedtakXml(lagretVedtakId, e).toException();
        }
    }


}
