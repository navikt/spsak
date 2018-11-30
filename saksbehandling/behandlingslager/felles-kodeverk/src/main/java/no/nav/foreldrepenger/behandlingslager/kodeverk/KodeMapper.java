package no.nav.foreldrepenger.behandlingslager.kodeverk;

import no.nav.vedtak.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Hjelpeklasse som tilbyr bygging av en mapping mellom kodelisteinnslag og noe annet.
 *
 * Kodelisteinnslag kan ikke brukes som case i en switch, så denne klassen er et kompakt og effektivt alternativ
 * som også støtter toveis mapping.
 */
public class KodeMapper<K extends Kodeliste, O> {
    private final List<Tuple<K, O>> mappinger;

    private KodeMapper(List<Tuple<K, O>> mappinger) {
        this.mappinger = Collections.unmodifiableList(mappinger);
    }

    public Optional<O> map(K k) {
        return mappinger.stream()
            .filter(mapping -> mapping.getElement1().equals(k))
            .map(Tuple::getElement2)
            .findAny();
    }

    public Optional<K> omvendtMap(O o) {
        return mappinger.stream()
            .filter(mapping -> mapping.getElement2().equals(o))
            .map(Tuple::getElement1)
            .findAny();
    }

    public static <R extends Kodeliste, T> Builder<R, T> medMapping(R r, T t) {
        return new Builder<R, T>().medMapping(r, t);
    }

    public static class Builder<R extends Kodeliste, T> {
        private final ArrayList<R> rs;
        private final ArrayList<T> ts;

        private Builder() {
            rs = new ArrayList<>();
            ts = new ArrayList<>();
        }

        public Builder<R, T> medMapping(R r, T t) {
            Objects.requireNonNull(r);
            Objects.requireNonNull(t);
            if (rs.contains(r)) {
                throw new IllegalArgumentException(String.format("Har allerede mapping for %s", r));
            }
            if (ts.contains(t)) {
                throw new IllegalArgumentException(String.format("Har allerede mapping for %s", t));
            }
            rs.add(r);
            ts.add(t);
            return this;
        }

        public KodeMapper<R, T> build() {
            return new KodeMapper<>(IntStream.range(0, rs.size())
                .mapToObj(i -> new Tuple<>(rs.get(i), ts.get(i)))
                .collect(Collectors.toList()));
        }
    }
}
