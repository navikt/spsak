package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher;

import java.util.Objects;

public class EditDistanceOperasjon<T extends EditDistanceLetter> {
    private final T før;
    private final T nå;

    EditDistanceOperasjon(T før, T nå) {
        this.før = før;
        this.nå = nå;
    }

    public T getFør() {
        return før;
    }

    public T getNå() {
        return nå;
    }

    public boolean erSettInnOperasjon() {
        return nå != null && før == null;
    }

    public boolean erSletteOperasjon() {
        return nå == null && før != null;
    }

    public boolean erEndreOperasjon() {
        return nå != null && før != null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EditDistanceOperasjon)) {
            return false;
        }

        EditDistanceOperasjon<?> that = (EditDistanceOperasjon<?>) o;
        return Objects.equals(før, that.før)
            && Objects.equals(nå, that.nå);
    }

    @Override
    public int hashCode() {
        return Objects.hash(før, nå);
    }

    @Override
    public String toString() {
        return "EditDistanceOperasjon{" +
            "før=" + før +
            ", nå=" + nå +
            '}';
    }
}
