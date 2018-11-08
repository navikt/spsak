package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.felles.integrasjon.dokument.produksjon.DokumentproduksjonConsumerConfig;

@Alternative
@Priority(1)
class DokumentproduksjonConsumerConfigMock extends DokumentproduksjonConsumerConfig {

    public DokumentproduksjonConsumerConfigMock() {
        super();
    }

}
