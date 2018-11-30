package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Kommunikasjonsretning {
    /**
     * Inngående dokument
     */
    INN("I"),
    /**
     * Utgående dokument
     */
    UT("U"),
    /**
     * Internt notat
     */
    NOTAT("N");

    private static final Map<String, Kommunikasjonsretning> KOMMUNIKASJONSRETNING_MAP;

    private String kommunikasjonsretningCode;

    static {
        Map<String, Kommunikasjonsretning> map = new ConcurrentHashMap<>();
        for (Kommunikasjonsretning kommunikasjonsretning : Kommunikasjonsretning.values()) {
            map.put(kommunikasjonsretning.getKommunikasjonsretningCode(), kommunikasjonsretning);
        }
        KOMMUNIKASJONSRETNING_MAP = Collections.unmodifiableMap(map);
    }

    Kommunikasjonsretning(String kommunikasjonsretningCode) {
        this.kommunikasjonsretningCode = kommunikasjonsretningCode;
    }

    public static Kommunikasjonsretning fromKommunikasjonsretningCode(String kommunikasjonsretningCode) {
        return KOMMUNIKASJONSRETNING_MAP.get(kommunikasjonsretningCode);
    }

    public String getKommunikasjonsretningCode() {
        return kommunikasjonsretningCode;
    }
}
