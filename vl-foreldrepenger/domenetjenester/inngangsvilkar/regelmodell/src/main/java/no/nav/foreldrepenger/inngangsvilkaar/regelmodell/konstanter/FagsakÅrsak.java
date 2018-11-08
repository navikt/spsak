package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter;

public enum FagsakÅrsak {
    FØDSEL("Fødsel"),
    ADOPSJON("Adopsjon"),
    OMSORG("Omsorgsovertakelse");

    private String kode;

    FagsakÅrsak(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
