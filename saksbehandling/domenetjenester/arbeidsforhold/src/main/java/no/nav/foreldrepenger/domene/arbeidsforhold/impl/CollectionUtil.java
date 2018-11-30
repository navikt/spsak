package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

final class CollectionUtil {

    private CollectionUtil() {
    }

    // Kan fjernes når kodebasen er oppgradert til java version > 8
    public static <T, U, A, R> Collector<T, A, R> flatMapping(
        Function<? super T, ? extends Stream<? extends U>> mapper,
        Collector<? super U, A, R> downstream) {

        BiConsumer<A, ? super U> acc = downstream.accumulator();
        return Collector.of(downstream.supplier(),
            (a, t) -> {
                try (Stream<? extends U> s = mapper.apply(t)) {
                    if (s != null) s.forEachOrdered(u -> acc.accept(a, u));
                }
            },
            downstream.combiner(), downstream.finisher(),
            downstream.characteristics().toArray(new Collector.Characteristics[0]));
    }
}
