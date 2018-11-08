package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class JsonMapper {
    private static final ObjectMapper mapper = getObjectMapper();

    public static String toJson(Object dto) {
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw JsonMapperFeil.FACTORY.kunneIkkeSerialisereJson(e).toException();
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw JsonMapperFeil.FACTORY.ioExceptionVedLesing(e).toException();
        }
    }


    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw JsonMapperFeil.FACTORY.ioExceptionVedLesing(e).toException();
        }
    }

    interface JsonMapperFeil extends DeklarerteFeil {

        JsonMapperFeil FACTORY = FeilFactory.create(JsonMapperFeil.class);

        @TekniskFeil(feilkode = "F-919328", feilmelding = "Fikk IO exception ved parsing av JSON", logLevel = LogLevel.WARN)
        Feil ioExceptionVedLesing(IOException cause);

        @TekniskFeil(feilkode = "F-208314", feilmelding = "Kunne ikke serialisere objekt til JSON", logLevel = LogLevel.WARN)
        Feil kunneIkkeSerialisereJson(JsonProcessingException cause);
    }

}





