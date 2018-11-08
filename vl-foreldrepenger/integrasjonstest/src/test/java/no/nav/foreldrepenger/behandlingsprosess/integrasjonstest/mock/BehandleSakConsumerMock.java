package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakResponse;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSakEksistererAlleredeException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSUgyldigInputException;
import no.nav.vedtak.felles.integrasjon.behandlesak.klient.BehandleSakConsumer;

@Dependent
@Alternative
@Priority(1)
class BehandleSakConsumerMock implements BehandleSakConsumer {

    private static int saksnummer = 1;


    @Override
    public WSOpprettSakResponse opprettSak(WSOpprettSakRequest request) throws WSSikkerhetsbegrensningException, WSSakEksistererAlleredeException, WSUgyldigInputException {
        WSOpprettSakResponse response = new WSOpprettSakResponse();
        response.setSakId(String.valueOf(saksnummer++));
        return response;
    }
}
