package no.nav.foreldrepenger.domene.vedtak.innsyn;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.domene.vedtak.innsyn.parser.DocumentParser;
import no.nav.foreldrepenger.domene.vedtak.innsyn.parser.impl.DocumentParserV1;
import no.nav.foreldrepenger.domene.vedtak.innsyn.parser.impl.DocumentParserV2;

public class DocumentParserProvider {

    private static Map<String, DocumentParser> transformers = Collections.unmodifiableMap(Stream.of(
        new AbstractMap.SimpleEntry<>(no.nav.foreldrepenger.vedtak.v1.ForeldrepengerVedtakConstants.NAMESPACE, new DocumentParserV1()),
        new AbstractMap.SimpleEntry<>(no.nav.foreldrepenger.vedtak.v2.VedtakConstants.NAMESPACE, new DocumentParserV2()))
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

    private DocumentParserProvider() {
    }

    public static DocumentParser get(Long lagretVedtakId, String namespace) {
        if (!transformers.containsKey(namespace)) {
            throw TransformerVedtakXmlFeil.FACTORY.ukjentNamespace(lagretVedtakId, namespace).toException();
        }
        return transformers.get(namespace);
    }
}
