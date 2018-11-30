package no.nav.foreldrepenger.web.app.tjenester.behandling;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import no.nav.foreldrepenger.web.app.jackson.JacksonJsonConfig;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Map object fra/til json.
 */
public class JsonUtil {

    private final JacksonJsonConfig jsonConfig = new JacksonJsonConfig();
    private final ObjectMapper mapper = jsonConfig.getObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory(mapper);
    private Class<?> elementClass;
    private CollectionLikeType collectionLikeType;

    public JsonUtil(Class<?> elementClass) {
        this.elementClass = elementClass;
        this.collectionLikeType = TypeFactory.defaultInstance().constructCollectionLikeType(List.class, elementClass);
    }

    public String toJsonString(Object obj) throws IOException {
        StringWriter w = new StringWriter(1000);
        toJson(obj, w);
        return w.toString();
    }

    public void toJson(Object obj, Writer w) throws IOException {
        try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(w);) {
            jsonGenerator.writeObject(obj);
            jsonGenerator.flush();
        }
    }

    @SuppressWarnings("unchecked")
    public <V> List<V> fromJsonList(String json) throws IOException {
        return (List<V>) mapper.readValue(json, collectionLikeType);
    }

    @SuppressWarnings("unchecked")
    public <V> List<V> fromJsonSimple(String json) throws IOException {
        return (List<V>) mapper.readValue(json, elementClass);
    }

    @SuppressWarnings("unchecked")
    public <V> V fromJson(String json, TypeReference<V> typeRef) throws IOException {
        return (V) mapper.readValue(json, typeRef);
    }
}
