package no.nav.foreldrepenger.kodeverk.api;

import java.time.LocalDate;
import java.util.Objects;

public class KodeverkInfo {

    private String eier;
    private String navn;
    private String versjon;
    private String uri;
    private LocalDate versjonDato;

    public String getEier(){
        return eier;
    }

    public String getVersjon() {
        return versjon;
    }

    public String getNavn() {
        return navn;
    }

    public String getUri() {
        return uri;
    }

    public LocalDate getVersjonDato() {
        return versjonDato;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KodeverkInfo that = (KodeverkInfo) o;
        return Objects.equals(eier, that.eier) &&
            Objects.equals(navn, that.navn) &&
            Objects.equals(versjon, that.versjon) &&
            Objects.equals(uri, that.uri) &&
            Objects.equals(versjonDato, that.versjonDato);
    }

    @Override
    public int hashCode() {

        return Objects.hash(eier, navn, versjon, uri, versjonDato);
    }

    public static class Builder {
        private KodeverkInfo kodeMal = new KodeverkInfo();

        public Builder medEier(String eier) {
            kodeMal.eier = eier;
            return this;
        }

        public Builder medVersjon(String versjon) {
            kodeMal.versjon = versjon;
            return this;
        }

        public Builder medNavn(String navn) {
            kodeMal.navn = navn;
            return this;
        }

        public Builder medUri(String uri) {
            kodeMal.uri = uri;
            return this;
        }

        public Builder medVersjonDato(LocalDate dato) {
            kodeMal.versjonDato = dato;
            return this;
        }

        public KodeverkInfo build(){
            return kodeMal;
        }
    }
}
