package no.nav.foreldrepenger.domene.vedtakinnsyn;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlTransformerProvider {

    private static Map<String, String> transformers = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>(no.nav.foreldrepenger.vedtak.v1.ForeldrepengerVedtakConstants.NAMESPACE, "vedtakXmlTilHtml_v1.xsl"),
            new AbstractMap.SimpleEntry<>(no.nav.foreldrepenger.vedtak.v2.ForeldrepengerVedtakConstants.NAMESPACE, "vedtakXmlTilHtml_v2.xsl"))
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));

    private HtmlTransformerProvider() {
    }

    public static String get(String namespace) {
        return transformers.getOrDefault(namespace, "vedtakXmlTilHtml_v2.xsl"); //Vi defaulter til versjon 2 da den skal være generell og forhåpentligvis også fungere med framtidige versjoner
    }
}
