package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JPA konverterer for å skrive ned en key=value text til et databasefelt (output tilsvarer java.util.Properties
 * format). Merk at objekter annet enn String skrives ned som Json struktur
 */
@Converter
public class PropertiesToJsonConverter implements AttributeConverter<Properties, String> {

    private static final ObjectMapper OM;

    static {
        OM = new ObjectMapper();

        OM.registerModule(new JavaTimeModule());
        OM.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
        OM.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
        OM.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    }

    @Override
    public String convertToDatabaseColumn(Properties props) {
        if (props == null || props.isEmpty()) {
            return null;
        }
        StringWriter sw = new StringWriter(512);
        if (props.values().stream().allMatch(s -> s instanceof String)) {
            // custom istdf Properties.store slik at vi ikke får med default timestamp
            props.forEach((k, v) -> {
                sw.append((String) k).append('=').append((String) v).append('\n');
            });
        } else {
            try {
                OM.writerWithDefaultPrettyPrinter().writeValue(sw, props);
            } catch (IOException e) {
                throw new IllegalArgumentException("Kunne ikke serialiseres til json: " + props, e);
            }
        }
        return sw.toString();

    }

    @Override
    public Properties convertToEntityAttribute(String dbData) {
        Properties props = new Properties();

        if (dbData != null) {
            // try string
            if (dbData.startsWith("{")) {
                try {
                    OM.readerFor(Properties.class).readValue(dbData);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Kunne ikke deserialiseres fra json: " + dbData, e);
                }
            } else {
                // try string
                try {
                    props.load(new StringReader(dbData));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Kan ikke lese properties til string:" + props, e); //$NON-NLS-1$
                }
            }
        }
        return props;
    }
}
