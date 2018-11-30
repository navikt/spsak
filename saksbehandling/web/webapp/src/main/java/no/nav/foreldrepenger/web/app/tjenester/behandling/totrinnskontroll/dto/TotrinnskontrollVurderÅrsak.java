package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto;

public class TotrinnskontrollVurderÅrsak {
    private String kode;
    private String navn;

    public TotrinnskontrollVurderÅrsak(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public String getKode() {
        return kode;
    }

    public String getNavn() {
        return navn;
    }
}
