package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.util.Objects;

public class Organisasjon implements Arbeidsgiver {
    private String orgNummer;

    public Organisasjon(String orgNummer) {
        this.orgNummer = orgNummer;
    }

    @Override
    public Organisasjon getArbeidsgiver() {
        return this;
    }

    @Override
    public String getIdentifikator() {
        return orgNummer;
    }

    public String getOrgNummer() {
        return orgNummer;
    }

    @Override
    public String toString() {
        return "Organisasjon{" +
            "orgNummer='" + orgNummer + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organisasjon that = (Organisasjon) o;
        return Objects.equals(orgNummer, that.orgNummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgNummer);
    }

    public static class Builder {
        private String orgNummer;

        public Builder medOrgNummer(String orgNummer) {
            this.orgNummer = orgNummer;
            return this;
        }

        public Organisasjon build() {
            return new Organisasjon(orgNummer);
        }

    }
}
