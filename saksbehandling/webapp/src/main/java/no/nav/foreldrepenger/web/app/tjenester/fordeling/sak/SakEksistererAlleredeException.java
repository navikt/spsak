package no.nav.foreldrepenger.web.app.tjenester.fordeling.sak;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;

public class SakEksistererAlleredeException extends TekniskException {
    public SakEksistererAlleredeException(Feil feil) {
        super(feil);
    }
}
