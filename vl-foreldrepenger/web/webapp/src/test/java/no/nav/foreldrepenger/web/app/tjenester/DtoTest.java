package no.nav.foreldrepenger.web.app.tjenester;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Base klasse for dto testing. Legger til serializere og deserializere slik at dto'er med custom typer kan testes.
 */
public abstract class DtoTest {

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        module.addSerializer(LocalDate.class, new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer(formatter));
        module.addDeserializer(LocalDate.class, new com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer(formatter));
        objectMapper.registerModule(module);
    }

}
