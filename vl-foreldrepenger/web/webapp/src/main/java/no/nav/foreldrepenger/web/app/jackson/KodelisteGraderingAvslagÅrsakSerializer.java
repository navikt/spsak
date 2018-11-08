package no.nav.foreldrepenger.web.app.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import no.nav.foreldrepenger.behandlingslager.uttak.GraderingAvslagÅrsak;

/**
 * Enkel serialisering av KodeverkTabell klass GraderingAvslagÅrsak, uten at disse trenger @JsonIgnore eller lignende. Deserialisering går
 * av seg selv normalt (får null for andre felter).
 */
public class KodelisteGraderingAvslagÅrsakSerializer extends StdSerializer<GraderingAvslagÅrsak> {

    public KodelisteGraderingAvslagÅrsakSerializer() {
        super(GraderingAvslagÅrsak.class);
    }

    @Override
    public void serialize(GraderingAvslagÅrsak value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        jgen.writeStringField("kode", value.getKode());
        jgen.writeStringField("navn", value.getNavn());
        jgen.writeStringField("kodeverk", value.getKodeverk());
        jgen.writeStringField("gyldigFom", value.getGyldigFraOgMed().toString());
        jgen.writeStringField("gyldigTom", value.getGyldigTilOgMed().toString());

        jgen.writeEndObject();
    }

}
