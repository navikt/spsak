package no.nav.foreldrepenger.batch;

import no.nav.foreldrepenger.batch.feil.BatchFeil;

import java.util.Map;

public abstract class BatchArguments {

    public BatchArguments(Map<String, String> arguments) {
        arguments.entrySet().removeIf(it -> settParameterVerdien(it.getKey(), it.getValue()));

        if(!arguments.entrySet().isEmpty()) {
            throw BatchFeil.FACTORY.ukjenteJobParametere(arguments.keySet()).toException();
        }
    }

    public abstract boolean settParameterVerdien(String key, String value);

    /**
     * @return sant / usant om argumentene er semtantisk korrekte
     */
    public abstract boolean isValid();
}
