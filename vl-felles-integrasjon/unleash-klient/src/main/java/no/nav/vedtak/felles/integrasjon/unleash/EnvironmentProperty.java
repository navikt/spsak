package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.Optional;

import no.nav.vedtak.konfig.PropertyUtil;

public class EnvironmentProperty {

    public static final String FASIT_ENVIRONMENT_NAME = "FASIT_ENVIRONMENT_NAME";

    private EnvironmentProperty() {}

    public static Optional<String> getEnvironmentName() {
        String environmentName = PropertyUtil.getProperty("environment.name");
        if (environmentName != null) {
            return Optional.of(environmentName);
        }
        return Optional.ofNullable(PropertyUtil.getProperty(FASIT_ENVIRONMENT_NAME));
    }
}
