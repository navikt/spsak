package no.nav.foreldrepenger.kodeverk.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KodeverkKode {

    private String kodeverk;
    private String kode;
    private String navn;
    private String uri;
    private LocalDate gyldigFom;
    private LocalDate gyldigTom;
    private List<KodeverkKode> underkoder = new ArrayList<>();

    public String getKodeverk(){
        return kodeverk;
    }

    public String getKode() {
        return kode;
    }

    public String getNavn() {
        return navn;
    }

    public String getUri() {
        return uri;
    }

    public LocalDate getGyldigFom() {
        return gyldigFom;
    }

    public LocalDate getGyldigTom() {
        return gyldigTom;
    }

    public List<KodeverkKode> getUnderkoder() {
        return underkoder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof KodeverkKode)) {
            return false;
        }
        KodeverkKode other = (KodeverkKode) obj;
        return Objects.equals(this.gyldigFom, other.gyldigFom)
            && Objects.equals(this.gyldigTom, other.gyldigTom)
            && Objects.equals(this.kode, other.kode)
            && Objects.equals(this.navn, other.navn)
            && Objects.equals(this.uri, other.uri)
            && Objects.equals(this.kodeverk, other.kodeverk)
            && Objects.equals(this.underkoder, other.underkoder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeverk, kode, navn, uri, gyldigFom, gyldigTom, underkoder);
    }

    public static class Builder {
        private KodeverkKode kodeMal = new KodeverkKode();

        public Builder medKodeverk(String kodeverk) {
            kodeMal.kodeverk = kodeverk;
            return this;
        }

        public Builder medKode(String kode) {
            kodeMal.kode = kode;
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

        public Builder medGyldigFom(LocalDate fom) {
            kodeMal.gyldigFom = fom;
            return this;
        }

        public Builder medGyldigTom(LocalDate tom) {
            kodeMal.gyldigTom = tom;
            return this;
        }

        public Builder leggTilUnderkoder(List<KodeverkKode> underkoder){
            kodeMal.underkoder.addAll(underkoder);
            return this;
        }

        public KodeverkKode build(){
            return kodeMal;
        }
    }
}
