package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

public enum Meldingstype {
    INFOTRYGD_INNVILGET("INNVILGET_v1", InfotrygdInnvilget.class),
    INFOTRYGD_ENDRET("ENDRET_v1", InfotrygdEndret.class),
    INFOTRYGD_OPPHOERT("OPPHOERT_v1", InfotrygdOpphørt.class),
    INFOTRYGD_ANNULLERT("ANNULLERT_v1", InfotrygdAnnulert.class);

    private Class<? extends Innhold> meldingsDto;
    private String type;

    Meldingstype(String type, Class<? extends Innhold> meldingsDto) {
        this.meldingsDto = meldingsDto;
        this.type = type;
    }

    public Class<? extends Innhold> getMeldingsDto() {
        return this.meldingsDto;
    }

    public String getType() {
        return this.type;
    }

}
