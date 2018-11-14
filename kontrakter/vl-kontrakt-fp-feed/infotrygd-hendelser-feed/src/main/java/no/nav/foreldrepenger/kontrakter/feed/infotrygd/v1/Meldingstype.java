package no.nav.foreldrepenger.kontrakter.feed.infotrygd.v1;


import java.util.Arrays;

public enum Meldingstype {
	INFOTRYGD_INNVILGET("INNVILGET_v1", InfotrygdInnvilget.class),
    INFOTRYGD_ENDRET("ENDRET_v1", InfotrygdEndret.class),
    INFOTRYGD__OPPHOERT("OPPHOERT_v1", InfotrygdOpphoert.class),
    INFOTRYGD_ANNULLERT("ANNULLERT_v1", InfotrygdAnnullert.class);

    private Class<? extends Innhold> meldingsDto;
    private String type;

    Meldingstype(String type, Class<? extends Innhold> meldingsDto) {
        this.meldingsDto = meldingsDto;
        this.type = type;
    }
    
    @SuppressWarnings("unchecked")
    public <V extends Innhold> Class<V> getMeldingsDto() {
        return (Class<V>) meldingsDto;
    }
    
    public String getType() {
        return type;
    }

    public static Meldingstype valueOf(Class<? extends Innhold> aClass) {
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