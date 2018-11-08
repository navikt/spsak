package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter;

public enum SoekerRolle {
    MORA("Mor til"),
    MEDMOR("Medmor til"),
    FARA("Far til");

    private String kode;

    SoekerRolle(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
