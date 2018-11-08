package no.nav.foreldrepenger.kontrakter.feed.infotrygd.v1;


import java.util.Arrays;

public enum Meldingstype {
	INFOTRYGD_INNVILGET("INNVILGET_v1", InfotrygdInnvilget.class),
    INFOTRYGD_ENDRET("ENDRET_v1", InfotrygdEndret.class),
    INFOTRYGD__OPPHOERT("OPPHOERT_v1", InfotrygdOpphoert.class),
    INFOTRYGD_ANNULLERT("ANNULLERT_v1", InfotrygdAnnullert.class);

    private Class meldingsDto;
    private String type;

    Meldingstype(String type, Class meldingsDto) {
        this.meldingsDto = meldingsDto;
        this.type = type;
    }
    
    public Class getMeldingsDto() {
        return meldingsDto;
    }
    
    public String getType() {
        return type;
    }

    public static Meldingstype valueOf(Class aClass) {
        return Arrays.stream(values())
                .filter(e-> e.getMeldingsDto().equals(aClass))
                .findFirst().orElse(null);
    }
    
    public static Meldingstype fromType(String type) {
        return Arrays.stream(values())
                .filter(e-> e.getType().equals(type))
                .findFirst().orElse(null);
    }
}