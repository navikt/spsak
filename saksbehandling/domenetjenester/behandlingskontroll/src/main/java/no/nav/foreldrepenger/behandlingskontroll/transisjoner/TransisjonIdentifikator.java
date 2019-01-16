package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import java.util.Objects;

public final class TransisjonIdentifikator {

    private final String id;

    private TransisjonIdentifikator(String id) {
        this.id = id;
    }

    public static TransisjonIdentifikator forId(String id) {
        return new TransisjonIdentifikator(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "TransisjonIdentifikator{" +
            "id='" + id + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransisjonIdentifikator)) {
            return false;
        }

        TransisjonIdentifikator that = (TransisjonIdentifikator) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
