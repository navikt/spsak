package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdConsumerConfig;

@Alternative
@Priority(1)
class ArbeidsforholdConsumerConfigMock extends ArbeidsforholdConsumerConfig {
    public ArbeidsforholdConsumerConfigMock() {
        super(null);
    }
}
