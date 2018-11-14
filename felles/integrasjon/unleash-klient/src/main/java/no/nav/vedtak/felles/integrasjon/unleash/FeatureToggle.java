package no.nav.vedtak.felles.integrasjon.unleash;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import no.finn.unleash.Unleash;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface FeatureToggle {
    Annotation TYPE_LITERAL = new FeatureToggle.FeatureToggleTypeLiteral();

    /* Nøkkel for å slå opp verdi. */
    @Nonbinding
    String value() default "";

    @Nonbinding
    boolean required() default true;

    @Nonbinding
    Class<? extends FeatureToggle.Converter> converter() default FeatureToggle.UnleashConverter.class;

    interface Converter {
        Unleash tilUnleash(String appName);
    }

    class UnleashConverter implements FeatureToggle.Converter {

        @Override
        public Unleash tilUnleash(String appName) {
            ToggleConfig toggleConfig = new ToggleConfig(appName);
            return toggleConfig.unleash();
        }
    }

    class FeatureToggleTypeLiteral extends AnnotationLiteral<FeatureToggle> implements FeatureToggle {

        @Override
        public String value() {
            return null;
        }

        @Override
        public boolean required() {
            return true;
        }

        @Override
        public Class<? extends Converter> converter() {
            return UnleashConverter.class;
        }
    }
}
