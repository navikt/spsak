package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Permisjon {

    private LocalDate permisjonFom;
    private LocalDate permisjonTom;
    private BigDecimal permisjonsprosent;
    private String permisjonsÅrsak;

    private Permisjon(LocalDate permisjonFom, LocalDate permisjonTom, BigDecimal permisjonsprosent, String permisjonsÅrsak) {
        this.permisjonFom = permisjonFom;
        this.permisjonTom = permisjonTom;
        this.permisjonsprosent = permisjonsprosent;
        this.permisjonsÅrsak = permisjonsÅrsak;
    }

    public LocalDate getPermisjonFom() {
        return permisjonFom;
    }

    public LocalDate getPermisjonTom() {
        return permisjonTom;
    }

    public BigDecimal getPermisjonsprosent() {
        return permisjonsprosent;
    }

    public String getPermisjonsÅrsak() {
        return permisjonsÅrsak;
    }

    public static class Builder {
        private LocalDate permisjonFom;
        private LocalDate permisjonTom;
        private BigDecimal permisjonsprosent;
        private String permisjonsÅrsak;

        public Permisjon.Builder medPermisjonFom(LocalDate permisjonFom) {
            this.permisjonFom = permisjonFom;
            return this;
        }

        public Permisjon.Builder medPermisjonTom(LocalDate permisjonTom) {
            this.permisjonTom = permisjonTom;
            return this;
        }

        public Permisjon.Builder medPermisjonsprosent(BigDecimal permisjonsprosent) {
            this.permisjonsprosent = permisjonsprosent;
            return this;
        }

        public Permisjon.Builder medPermisjonsÅrsak(String permisjonsÅrsak) {
            this.permisjonsÅrsak = permisjonsÅrsak;
            return this;
        }
        public Permisjon build() {
            return new Permisjon(permisjonFom, permisjonTom, permisjonsprosent, permisjonsÅrsak);
        }
    }
}
