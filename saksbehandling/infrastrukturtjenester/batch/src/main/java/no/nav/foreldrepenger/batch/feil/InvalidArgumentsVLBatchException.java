package no.nav.foreldrepenger.batch.feil;

import no.nav.vedtak.feil.Feil;

public class InvalidArgumentsVLBatchException extends VLBatchException {

    public InvalidArgumentsVLBatchException(Feil feil) {
        super(feil);
    }
}
