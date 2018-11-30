package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * Marker type som implementerer interface {@link StartpunktUtleder}.
 */
@Qualifier
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Documented
public @interface GrunnlagRef {

    /**
     * Settes til navn på forretningshendelse slik det defineres i KODELISTE-tabellen.
     */
    String value();

    /** AnnotationLiteral som kan brukes ved CDI søk. */
    class GrunnlagRefLiteral extends AnnotationLiteral<GrunnlagRef> implements GrunnlagRef {

        private String navn;

        public GrunnlagRefLiteral(String navn) {
            this.navn = navn;
        }

        @Override
        public String value() {
            return navn;
        }
    }
}
