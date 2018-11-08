package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;
import no.nav.vedtak.felles.integrasjon.sak.SakConsumer;

@Dependent
@Alternative
@Priority(1)
class SakConsumerMock implements SakConsumer {

    @Override
    public FinnSakResponse finnSak(FinnSakRequest finnSakRequest) throws FinnSakForMangeForekomster, FinnSakUgyldigInput {
        return new FinnSakResponse();
    }
}
