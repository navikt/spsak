package no.nav.foreldrepenger.behandlingslager.diff;

/**
 * Typisk utvikling exception når deler av grafen ikke kan initialiseres (eks. hibernate LazyInitializationException)
 */
public class TraverseEntityGraphException extends RuntimeException {

    public TraverseEntityGraphException(String message, Throwable t) {
        super(message, t);
    }
    
    public TraverseEntityGraphException(String message) {
        super(message);
    }
}
