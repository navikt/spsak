package no.nav.vedtak.felles.integrasjon.unleash;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;

import no.finn.unleash.Unleash;

@ApplicationScoped
public class FeatureToggleProdusent {

    public FeatureToggleProdusent() {
    }

    @FeatureToggle
    @Produces
    public Unleash getUnleash(InjectionPoint ip) {
        FeatureToggle annotation = getAnnotation(ip);
        String appName = annotation.value();
        FeatureToggle.Converter converter = new FeatureToggle.UnleashConverter();
        return converter.tilUnleash(appName);
    }

    private FeatureToggle getAnnotation(final InjectionPoint ip) {
        Annotated annotert = ip.getAnnotated();
        if (annotert == null) {
            throw new IllegalArgumentException("Mangler annotation FeatureToggle for InjectionPoint=" + ip); //$NON-NLS-1$
        }
        if (annotert.isAnnotationPresent(FeatureToggle.class)) {
            FeatureToggle annotation = annotert.getAnnotation(FeatureToggle.class);
            if (!annotation.value().isEmpty()) {
                return annotation;
            }
        }
        throw new IllegalStateException("Mangler key. Kan ikke være tom eller null: " + ip.getMember()); //$NON-NLS-1$
    }
}
