package no.nav.foreldrepenger.uttaksvilkår.feil;

public class UttakRegelFeil extends RuntimeException {

    public UttakRegelFeil(String message, Throwable cause) {
        super(message, cause);
    }
}
