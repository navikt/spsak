package no.nav.foreldrepenger.behandlingslager.aktør;

public class Statsborgerskap {

    private String landkode;

    public Statsborgerskap(String landkode){
        this.landkode = landkode;
    }

    public String getLandkode() {
        return landkode;
    }

    public void setLandkode(String landkode) {
        this.landkode = landkode;
    }
}
