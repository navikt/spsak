package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.journal.v3.JournalConsumerConfig;

@Alternative
@Priority(1)
class JournalConsumerConfigMock extends JournalConsumerConfig {

    public JournalConsumerConfigMock() {
        super(null);
    }

}
