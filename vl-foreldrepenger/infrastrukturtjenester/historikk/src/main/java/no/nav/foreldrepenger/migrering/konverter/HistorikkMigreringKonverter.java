package no.nav.foreldrepenger.migrering.konverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.migrering.HistorikkMigreringFeil;

public interface HistorikkMigreringKonverter {

    List<HistorikkinnslagDel> konverter(Historikkinnslag historikkinnslag);

    @SuppressWarnings("deprecation")
    static JsonObject parseJSON(Historikkinnslag innslag) {
        String tekst = innslag.getTekst();
        InputStream inputStream = new ByteArrayInputStream(tekst.getBytes());// NOSONAR
        try (JsonReader reader = Json.createReader(inputStream)) {
            return reader.readObject();
        }
    }

    static String getNullableString(JsonObject object, String key) {
        JsonValue value = object.get(key);
        if (value == null || value.getValueType() == JsonValue.ValueType.NULL) {
            return null;
        }
        if (value.getValueType() == JsonValue.ValueType.TRUE || value.getValueType() == JsonValue.ValueType.FALSE) {
            boolean boolValue = object.getBoolean(key);
            return Boolean.toString(boolValue);
        }
        if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            JsonNumber number = object.getJsonNumber(key);
            return Integer.toString(number.intValue());
        }
        if (value.getValueType() != JsonValue.ValueType.STRING) {
            throw HistorikkMigreringFeil.FACTORY.uventetJsonValueType(value.getValueType().toString()).toException();
        }
        JsonString string = (JsonString) value;
        return string.getString();
    }
}
