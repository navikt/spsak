package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.person.PersonConsumerConfig;

@Alternative
@Priority(1)
class PersonConsumerConfigMock extends PersonConsumerConfig {
    public PersonConsumerConfigMock() {
        super(null);
    }
}
