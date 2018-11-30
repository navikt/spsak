package no.nav.foreldrepenger.behandlingslager.aktør;

public class GeografiskTilknytning {

    private String tilknytning;
    private String diskresjonskode;

    public GeografiskTilknytning(String geografiskTilknytning, String diskresjonskode) {
        this.tilknytning = geografiskTilknytning;
        this.diskresjonskode = diskresjonskode;
    }

    public String getTilknytning() {
        return tilknytning;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }
}
