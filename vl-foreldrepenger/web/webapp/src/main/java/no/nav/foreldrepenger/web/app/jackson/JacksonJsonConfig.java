package no.nav.foreldrepenger.web.app.jackson;

import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.web.app.IndexClasses;

@Provider
public class JacksonJsonConfig implements ContextResolver<ObjectMapper> {

    private static final SimpleModule SER_DESER = createModule();
    private final ObjectMapper objectMapper;

    public JacksonJsonConfig() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // TODO (u139158): PK-44270 Diskutere med Front-end, ønsker i utgangpunktet å fjerne null, men hva med Javascript
        // KodelisteSerializer og KodeverkSerializer bør i tilfelle også støtte JsonInclude.Include.*
        // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(SER_DESER);

        objectMapper.registerSubtypes(getJsonTypeNameClasses());
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static Module defaultModule() {
        return SER_DESER;
    }

    private static SimpleModule createModule() {
        SimpleModule module = new SimpleModule("VL-REST", new Version(1, 0, 0, null, null, null));

        addSerializers(module);

        return module;
    }

    private static void addSerializers(SimpleModule module) {
        module.addSerializer(new KodeverkSerializer());
        module.addSerializer(new KodelisteSerializer());
        module.addSerializer(new StringSerializer());
        module.addSerializer(new KodelistePeriodeResultatÅrsakSerializer());
        module.addSerializer(new KodelisteGraderingAvslagÅrsakSerializer());
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

    /** Scan subtyper dynamisk fra WAR slik at superklasse slipper å deklarere @JsonSubtypes. */
    public static List<Class<?>> getJsonTypeNameClasses() {
        Class<JacksonJsonConfig> cls = JacksonJsonConfig.class;
        IndexClasses indexClasses;
        try {
            indexClasses = IndexClasses.getIndexFor(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
            return indexClasses.getClassesWithAnnotation(JsonTypeName.class);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Kunne ikke konvertere CodeSource location til URI", e);
        }
    }

}
