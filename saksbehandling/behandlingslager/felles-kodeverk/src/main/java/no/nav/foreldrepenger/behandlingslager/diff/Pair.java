package no.nav.foreldrepenger.behandlingslager.diff;

import java.util.Objects;

public class Pair {
    private final Object elem1;
    private final Object elem2;

    public Pair(Object elem1, Object elem2) {
        this.elem1 = elem1;
        this.elem2 = elem2;
    }

    public Object getElement1() {
        return elem1;
    }

    public Object getElement2() {
        return elem2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Pair))
            return false;

        Pair tuple = (Pair) o;

        return Objects.equals(elem1, tuple.elem1)
                && Objects.equals(elem2, tuple.elem2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elem1, elem2);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + elem1 + ", " + elem2 + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}