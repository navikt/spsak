package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumerConfig;

@Alternative
@Priority(1)
public class OrganisasjonConsumerConfigMock extends OrganisasjonConsumerConfig{
    public OrganisasjonConsumerConfigMock() {
        super(null);
    }
}
