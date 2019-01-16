package no.nav.foreldrepenger.behandlingskontroll;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * Marker type som implementerer interface {@link BehandlingSteg} for å skille ulike implementasjoner av samme steg for ulike behandlingtyper.<br>
 *
 * NB: Settes kun dersom det er flere implementasjoner av med samme {@link BehandlingStegRef}.
 */
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface BehandlingTypeRef {

    /**
     * Kode-verdi som skiller ulike implementasjoner for ulike behandling typer.
     * <p>
     * Må matche ett innslag i <code>BEHANDling_TYPE</code> tabell for å kunne kjøres.
     *
     * @see no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType
     */
    String value() default "*";

    /** AnnotationLiteral som kan brukes ved CDI søk. */
    public static class BehandlingTypeRefLiteral extends AnnotationLiteral<BehandlingTypeRef> implements BehandlingTypeRef {

        private String navn;

        public BehandlingTypeRefLiteral() {
            this.navn = "*";
        }

        public BehandlingTypeRefLiteral(String navn) {
            this.navn = navn;
        }

        @Override
        public String value() {
            return navn;
        }
    }
}
