package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import no.nav.vedtak.felles.integrasjon.medl.MedlemConsumerConfig;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(1)
public class MedlemConsumerConfigMock extends MedlemConsumerConfig {
    public MedlemConsumerConfigMock() {
        super(null);
    }
}
