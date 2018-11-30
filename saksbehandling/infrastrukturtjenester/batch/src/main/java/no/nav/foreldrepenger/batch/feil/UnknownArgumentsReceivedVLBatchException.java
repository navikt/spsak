package no.nav.foreldrepenger.batch.feil;

import no.nav.vedtak.feil.Feil;

public class UnknownArgumentsReceivedVLBatchException extends VLBatchException {

    public UnknownArgumentsReceivedVLBatchException(Feil feil) {
        super(feil);
    }
}
