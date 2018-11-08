package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import no.nav.vedtak.felles.integrasjon.inntekt.InntektConsumerConfig;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(1)
public class InntektConsumerConfigMock extends InntektConsumerConfig {
    public InntektConsumerConfigMock() {
        super(null);
    }
}
