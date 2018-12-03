package no.nav.foreldrepenger.domene.mottak.dokumentmottak.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonJsonConfig {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(createModule());
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
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
    }
}
