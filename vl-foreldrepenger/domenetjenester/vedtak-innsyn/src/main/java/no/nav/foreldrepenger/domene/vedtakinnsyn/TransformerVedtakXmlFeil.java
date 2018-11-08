package no.nav.foreldrepenger.domene.vedtakinnsyn;

import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface TransformerVedtakXmlFeil extends DeklarerteFeil {

    TransformerVedtakXmlFeil FACTORY = FeilFactory.create(TransformerVedtakXmlFeil.class);

    @TekniskFeil(feilkode = "FP-296812", feilmelding = "Fikk uventet feil ved transformasjon av VedtakXML for behandlingId '%s' til leselig format", logLevel = LogLevel.WARN)
    Feil feilVedTransformeringAvVedtakXml(Long behandlingId, TransformerConfigurationException cause);

    @TekniskFeil(feilkode = "FP-956702", feilmelding = "Fikk uventet feil ved transformasjon av VedtakXML for behandlingId '%s' til leselig format (linje %s kolonne %s). Forventet at kan skje ved transfomasjon av vedtak som er gjort på fundamentet.", logLevel = LogLevel.WARN)
    Feil feilVedTransformeringAvVedtakXml(Long behandlingId, Integer line, Integer columnNumber, TransformerException cause);

    @TekniskFeil(feilkode = "FP-566266", feilmelding = "VedtakXMl var ikke UTF-8-encodet for behandlingId '%s'", logLevel = LogLevel.WARN)
    Feil ioFeilVedTransformeringAvVedtakXml(Long behandlingId, UnsupportedEncodingException cause);

    @TekniskFeil(feilkode = "FP-116361", feilmelding = "VedtakXMl har et ukjent namespacee for behandlingId '%s'. Namespace of XML er '%s'.", logLevel = LogLevel.WARN)
    Feil ukjentNamespace(Long behandlingId, String nameSpaceOfXML);

    @TekniskFeil(feilkode = "FP-376155", feilmelding = "VedtakXMl validerer ikke mot xsd for behandlingId '%s'. Namespace of XML er '%s'.", logLevel = LogLevel.WARN)
    Feil vedtakXmlValiderteIkke(Long behandlingId, String nameSpaceOfXML, Exception cause);

}
