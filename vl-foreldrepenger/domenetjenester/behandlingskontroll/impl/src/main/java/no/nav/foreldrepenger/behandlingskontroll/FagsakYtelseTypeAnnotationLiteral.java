package no.nav.foreldrepenger.behandlingskontroll;

import javax.enterprise.util.AnnotationLiteral;

/**
 * AnnotationLiteral som kan brukes i CDI søk.
 * <p>
 * Eks. for bruk i:<br>
 * {@link CDI#current#select(javax.enterprise.util.TypeLiteral, java.lang.annotation.Annotation...)}.
 */
class FagsakYtelseTypeAnnotationLiteral extends AnnotationLiteral<FagsakYtelseTypeRef> implements FagsakYtelseTypeRef {

    private String kode;
    
    FagsakYtelseTypeAnnotationLiteral() {
        this.kode = "*";
    }
    
    FagsakYtelseTypeAnnotationLiteral(String stegKode) {
        this.kode = stegKode;
    }
    
    @Override
    public String value() {
        return kode;
    }
    
}