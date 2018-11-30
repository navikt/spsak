package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

/**
 * Håndterer serialisering/deserialisering av data strukturre til json for vilkår.
 * Kun felter vil serialiseres/deserialiseres, så endring i navn må en være forsiktig med (bør annoteres med
 * {@link JsonProperty} for å beskytte mot det)
 */
public class VilkårJsonObjectMapper {

    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();
        OM.registerModule(new JavaTimeModule());
        OM.registerModule(new Jdk8Module());
        OM.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
        OM.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
        OM.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OM.setVisibility(PropertyAccessor.CREATOR, Visibility.ANY);

        // Legacy support for gamle åpne behandlinger fra Fundamentet. Fjernes når disse er vedtatt og ferdig
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new OldLocalDateDeserializer());
        OM.registerModule(module);
    }

    public void write(Object data, Writer writer) {
        try {
            OM.writerWithDefaultPrettyPrinter().writeValue(writer, data);
        } catch (IOException e) {
            throw new IllegalArgumentException("Kunne ikke serialiseres til json: " + data, e);
        }
    }

    public <T> T readValue(String src, Class<T> targetClass) {
        try {
            return OM.readerFor(targetClass).readValue(src);
        } catch (IOException e) {
            throw new IllegalArgumentException("Kunne ikke deserialiser fra json til [" + targetClass.getName() + "]: " + src, e);
        }
    }
    
    public <T> T readValue(URL resource, Class<T> targetClass) {
        try {
            return OM.readerFor(targetClass).readValue(resource);
        } catch (IOException e) {
            throw new IllegalArgumentException("Kunne ikke deserialiser fra json til [" + targetClass.getName() + "]: " + resource.toExternalForm(), e);
        }
    }

    public String writeValueAsString(Object data) {
        StringWriter sw = new StringWriter(512);
        write(data, sw);
        return sw.toString();
    }

    /**
     * @deprecated tolker datao serialisert på merkelig måte i fundamentet (field dump). Kan fjernes når ikke flere åpne
     *             behandlinger fra Fundamentet i systemet.
     */
    @Deprecated
    static class OldLocalDateDeserializer extends StdDeserializer<LocalDate> {

        public OldLocalDateDeserializer() {
            this(null);
        }

        public OldLocalDateDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            try {
                // korrekt håndtering av datoer
                return LocalDateDeserializer.INSTANCE.deserialize(jsonParser, deserializationContext);
            } catch (MismatchedInputException e) {

                // Gammelt Legacy format(har ikke håndterert ikke java.time klasser ordentlig men dumper felter rett ut)
                JsonNode json = jsonParser.getCodec().readTree(jsonParser);
                JsonNode year = json.get("year");

                if (year == null) {
                    throw new IllegalArgumentException("Kan ikke deserialisere input hverken fra legacy format eller nytt forat", e);
                }
                JsonNode month = json.get("monthValue");
                JsonNode dayOfMonth = json.get("dayOfMonth");
                return LocalDate.of(year.asInt(), Month.of(month.intValue()), dayOfMonth.intValue());
            }
        }
    }

}
