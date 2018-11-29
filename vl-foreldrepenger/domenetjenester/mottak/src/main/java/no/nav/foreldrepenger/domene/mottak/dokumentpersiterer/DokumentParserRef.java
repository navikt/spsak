package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer;

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
 * Annotasjon for å merke klasser som brukes for oversetting av søknader.
 */
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface DokumentParserRef {

    /**
     * namespace av dokumentet
     */
    String value();

    /**
     * AnnotationLiteral som kan brukes ved CDI søk.
     */
    class DokumentParserRefLiteral extends AnnotationLiteral<DokumentParserRef> implements DokumentParserRef {

        private String value;

        public DokumentParserRefLiteral(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

    }

}
