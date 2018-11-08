package no.nav.foreldrepenger.web.app.tjenester.behandling;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.web.app.tjenester.behandling.SøknadType.SøknadTypeDeserializer;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = SøknadTypeDeserializer.class)
public enum SøknadType {
    FØDSEL("ST-001"), //$NON-NLS-1$
    ADOPSJON("ST-002"), //$NON-NLS-1$
    ;

    private static final String I18N_MELDINGER_KEY = "i18n.Meldinger"; //$NON-NLS-1$

    /** Default fil er samme som property key navn. */
    private static final String I18N_MELDINGER = System.getProperty(I18N_MELDINGER_KEY, I18N_MELDINGER_KEY);

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(I18N_MELDINGER); // $NON-NLS-1$

    private final String kode;

    private String navn;

    private SøknadType(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

    public String getNavn() {
        String str = this.navn;
        if (str == null) {
            str = BUNDLE.getString(getClass().getSimpleName() + "." + kode); //$NON-NLS-1$
            this.navn = str;
        }
        return str;
    }

    public static SøknadType fra(String kode) {
        for (SøknadType st : values()) {
            if (Objects.equals(st.kode, kode)) {
                return st;
            }
        }
        throw new IllegalArgumentException("Ukjent " + SøknadType.class.getSimpleName() + ": " + kode); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static SøknadType fra(FamilieHendelse type) {
        if (type == null) {
            return null;
        } else if (type.getGjelderFødsel()) {
            return SøknadType.FØDSEL;
        } else if (type.getGjelderAdopsjon()) {
            return SøknadType.ADOPSJON;
        } else {
            throw new IllegalArgumentException("Kan ikke mappe fra familieHendelse" + type + " til SøknadType"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    static class SøknadTypeDeserializer extends StdDeserializer<SøknadType> {

        public SøknadTypeDeserializer() {
            super(SøknadType.class);
        }

        @Override
        public SøknadType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // [JACKSON-620] Empty String can become null...

            if (p.hasToken(JsonToken.VALUE_STRING)
                    && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                    && p.getText().length() == 0) {
                return null;
            }

            String kode = null;

            if (Objects.equals(p.getCurrentToken(), JsonToken.START_OBJECT)) {
                while (!(JsonToken.END_OBJECT.equals(p.getCurrentToken()))) {
                    p.nextToken();
                    String name = p.getCurrentName();
                    String value = p.getValueAsString();
                    if (Objects.equals("kode", name)  && !Objects.equals("kode", value)) { //$NON-NLS-1$ //$NON-NLS-2$
                        kode = value;
                    }
                }
            }

            return SøknadType.fra(kode);
        }

    }


}

