package no.nav.foreldrepenger.batch.feil;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;

public class VLBatchException extends TekniskException {

    public VLBatchException(Feil feil) {
        super(feil);
    }
}
