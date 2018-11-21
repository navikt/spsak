package no.nav.foreldrepenger.domene.inngangsvilkaar;

public enum PersonStatusType {
    BOSA("Bosatt"),
    UTVA("Utvandret"),
    DØD("Død");

    private String kode;

    PersonStatusType(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

    public static PersonStatusType hentPersonStatusType(String kode) {
        for (PersonStatusType personStatusType : values()) {
            if (personStatusType.kode.equals(kode)) {
                return personStatusType;
            }
        }
        return null;
    }
}
