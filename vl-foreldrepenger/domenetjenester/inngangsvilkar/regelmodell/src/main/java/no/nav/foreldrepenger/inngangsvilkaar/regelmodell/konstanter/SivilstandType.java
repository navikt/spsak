package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter;

public enum SivilstandType {
    UGIFT("UGIFT"),
    GIFT("GIFT"),
    SAMBOER("SAMBOER"),
    PARTNER("PARTNER");

    private String kode;

    SivilstandType(String kode) {
        this.kode = kode;
    }

    public static SivilstandType hentSivilstandType(String kode) {
        for (SivilstandType sivilstandType : values()) {
            if (sivilstandType.kode.equals(kode)) {
                return sivilstandType;
            }
        }
        return null;
    }
}
