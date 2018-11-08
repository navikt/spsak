package no.nav.foreldrepenger.behandlingslager.aktør.historikk;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Personhistorikkinfo {

    private String aktørId;
    private List<PersonstatusPeriode> personstatushistorikk = new ArrayList<>();
    private List<StatsborgerskapPeriode> statsborgerskaphistorikk = new ArrayList<>();
    private List<AdressePeriode> adressehistorikk = new ArrayList<>();

    private Personhistorikkinfo() {
    }

    public String getAktørId() {
        return this.aktørId;
    }

    public List<PersonstatusPeriode> getPersonstatushistorikk() {
        return this.personstatushistorikk;
    }

    public List<StatsborgerskapPeriode> getStatsborgerskaphistorikk() {
        return this.statsborgerskaphistorikk;
    }

    public List<AdressePeriode> getAdressehistorikk() {
        return this.adressehistorikk;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Personhistorikkinfo{");
        sb.append("personstatushistorikk=").append(personstatushistorikk);
        sb.append(", statsborgerskaphistorikk=").append(statsborgerskaphistorikk);
        sb.append(", adressehistorikk=").append(adressehistorikk);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Personhistorikkinfo that = (Personhistorikkinfo) o;
        return Objects.equals(aktørId, that.aktørId) &&
            Objects.equals(personstatushistorikk, that.personstatushistorikk) &&
            Objects.equals(statsborgerskaphistorikk, that.statsborgerskaphistorikk) &&
            Objects.equals(adressehistorikk, that.adressehistorikk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId, personstatushistorikk, statsborgerskaphistorikk, adressehistorikk);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Personhistorikkinfo kladd;

        private Builder() {
            this.kladd = new Personhistorikkinfo();
        }

        public Builder medAktørId(String aktørId) {
            this.kladd.aktørId = aktørId;
            return this;
        }

        public Builder leggTil(PersonstatusPeriode personstatus) {
            this.kladd.personstatushistorikk.add(personstatus);
            return this;
        }

        public Builder leggTil(StatsborgerskapPeriode statsborgerskap) {
            this.kladd.statsborgerskaphistorikk.add(statsborgerskap);
            return this;
        }

        public Builder leggTil(AdressePeriode adresse) {
            this.kladd.adressehistorikk.add(adresse);
            return this;
        }

        public Personhistorikkinfo build() {
            requireNonNull(kladd.aktørId, "Personhistorikkinfo må ha aktørId"); //$NON-NLS-1$
            // TODO PK-49366 andre non-null?
            return kladd;
        }
    }
}
