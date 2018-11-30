package no.nav.foreldrepenger.batch;

import java.util.Map;

/**
 * Batchen tar ingen argumenter.
 */
public class EmptyBatchArguments extends BatchArguments {

    public EmptyBatchArguments(Map<String, String> arguments) {
        super(arguments);
    }

    @Override
    public boolean settParameterVerdien(String key, String value) {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
