package no.nav.foreldrepenger.web.app.tjenester.historikk.dto;

public class HistorikkInnslagGjeldendeFraDto {

    private String fra;
    private String navn;
    private String verdi;

    public HistorikkInnslagGjeldendeFraDto(String fra) {
        this.fra = fra;
    }

    public HistorikkInnslagGjeldendeFraDto(String fra, String navn, String verdi) {
        this.fra = fra;
        this.navn = navn;
        this.verdi = verdi;
    }


    public String getFra() {
        return fra;
    }

    public void setFra(String fra) {
        this.fra = fra;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getVerdi() {
        return verdi;
    }

    public void setVerdi(String verdi) {
        this.verdi = verdi;
    }
}
