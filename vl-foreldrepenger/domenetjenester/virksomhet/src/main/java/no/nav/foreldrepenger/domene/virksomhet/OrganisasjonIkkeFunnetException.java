package no.nav.foreldrepenger.domene.virksomhet;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class OrganisasjonIkkeFunnetException extends IntegrasjonException {
    public OrganisasjonIkkeFunnetException(Feil feil) {
        super(feil);
    }
}
