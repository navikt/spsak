package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid;

import java.util.Objects;

import no.nav.foreldrepenger.domene.typer.AktørId;

public class Person implements Arbeidsgiver {
    private AktørId aktørId;

    public Person(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    @Override
    public Person getArbeidsgiver() {
        return this;
    }

    @Override
    public String getIdentifikator() {
        return aktørId.getId();
    }

    public String getAktørId() {
        return aktørId.getId();
    }

    @Override
    public String toString() {
        return "Person{" +
            "aktørId=" + aktørId +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(aktørId, person.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    public static class Builder {
        private AktørId aktørId;

        public Builder medAktørId(AktørId aktørId) {
            this.aktørId = aktørId;
            return this;
        }

        public Person build() {
            return new Person(aktørId);
        }
    }
}
