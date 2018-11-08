package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;

@Alternative
@Priority(1)
public class AktørConsumerMedCacheMock extends AktørConsumerMedCache {
    @Inject
    private AktørConsumer aktørConsumer;

    public AktørConsumerMedCacheMock() {
        // Kun for å kunne initere superklasse. Superklassen er ikke i bruk
        super(null, 0, 0);
    }


    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        Optional<String> aktørId = aktørConsumer.hentAktørIdForPersonIdent(personIdent);
        return aktørId;
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        Optional<String> ident = aktørConsumer.hentPersonIdentForAktørId(aktørId);
        return ident;
    }

}
