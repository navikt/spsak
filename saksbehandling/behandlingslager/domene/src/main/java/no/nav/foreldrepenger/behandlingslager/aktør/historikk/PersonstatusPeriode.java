package no.nav.foreldrepenger.behandlingslager.aktør.historikk;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;

public class PersonstatusPeriode {

    private Gyldighetsperiode gyldighetsperiode;
    private PersonstatusType personstatus;

    public PersonstatusPeriode(Gyldighetsperiode gyldighetsperiode, PersonstatusType personstatus) {
        this.gyldighetsperiode = gyldighetsperiode;
        this.personstatus = personstatus;
    }

    public Gyldighetsperiode getGyldighetsperiode() {
        return this.gyldighetsperiode;
    }

    public PersonstatusType getPersonstatus() {
        return this.personstatus;
    }

    @Override
    public String toString() {
        return "PersonstatusPeriode(gyldighetsperiode=" + this.getGyldighetsperiode()
            + ", personstatus=" + this.getPersonstatus()
            + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonstatusPeriode that = (PersonstatusPeriode) o;
        return Objects.equals(gyldighetsperiode, that.gyldighetsperiode) &&
            Objects.equals(personstatus, that.personstatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gyldighetsperiode, personstatus);
    }
}
