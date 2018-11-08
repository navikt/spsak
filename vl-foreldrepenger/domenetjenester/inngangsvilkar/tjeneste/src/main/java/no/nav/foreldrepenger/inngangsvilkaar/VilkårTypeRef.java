package no.nav.foreldrepenger.inngangsvilkaar;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * Marker type som implementerer interface {@link Inngangsvilkår}.
 * Brukes for å konfigurere implementasjon av hvilke vilkår som skal kjøres.
 */
@Qualifier
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Documented
public @interface VilkårTypeRef {

    /**
     * Settes til navn på vilkår slik det defineres i VILKÅR_TYPE tabellen.
     */
    String value();

    /** AnnotationLiteral som kan brukes ved CDI søk. */
    public static class VilkårTypeRefLiteral extends AnnotationLiteral<VilkårTypeRef> implements VilkårTypeRef {

        private String navn;

        public VilkårTypeRefLiteral(String navn) {
            this.navn = navn;
        }

        @Override
        public String value() {
            return navn;
        }

    }

}
