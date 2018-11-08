package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class InfotrygdPersonIkkeFunnetException extends IntegrasjonException {
    public InfotrygdPersonIkkeFunnetException(Feil feil) {
        super(feil);
    }
}
