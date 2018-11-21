package no.nav.foreldrepenger.domene.inngangsvilkaar;

public enum Kjoenn {
    KVINNE("K"), MANN("M");

    private String kode;

    Kjoenn(String kode) {
        this.kode = kode;
    }

    public static Kjoenn hentKjoenn(String kode) {
        for (Kjoenn kjoenn : values()) {
            if (kjoenn.kode.equals(kode)) {
                return kjoenn;
            }
        }
        return null;
    }

    public String getKode() {
        return kode;
    }
}

