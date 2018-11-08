package no.nav.foreldrepenger.web.app.tjenester.saksbehandler.dto;

import java.util.Map;

public class FeatureToggleDto {

    private Map<String, Boolean> featureToggles;

    public FeatureToggleDto(Map<String, Boolean> featureToggles) {
        this.featureToggles = featureToggles;
    }

    public Map<String, Boolean> getFeatureToggles() {
        return featureToggles;
    }
}
