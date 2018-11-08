package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.GeografiskTilknytning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kommune;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumerProducer;
import no.nav.vedtak.felles.integrasjon.person.PersonSelftestConsumer;

@Alternative
@Priority(1)
@Dependent
public class PersonConsumerProducerMock extends PersonConsumerProducer {

    @Inject
    private RegisterKontekst registerKontekst;

    @Override
    public PersonConsumer personConsumer() {
        class PersonConsumerMock implements PersonConsumer {

            private PersonV3 portMock = new PersonServiceMockImpl(registerKontekst);

            @Override
            public HentPersonResponse hentPersonResponse(HentPersonRequest request) throws HentPersonSikkerhetsbegrensning, HentPersonPersonIkkeFunnet {
                return portMock.hentPerson(request);
            }

            @Override
            public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest request) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
                HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
                GeografiskTilknytning kommune = new Kommune();
                kommune.setGeografiskTilknytning("NITTEDAL");
                response.setGeografiskTilknytning(kommune);

                Diskresjonskoder diskresjonskode = new Diskresjonskoder();
                diskresjonskode.setValue("1");
                response.setDiskresjonskode(diskresjonskode);

                return response;
            }

            @Override
            public HentPersonhistorikkResponse hentPersonhistorikkResponse(HentPersonhistorikkRequest request)
                throws HentPersonhistorikkSikkerhetsbegrensning, HentPersonhistorikkPersonIkkeFunnet {

                return portMock.hentPersonhistorikk(request);
            }
        }
        return new PersonConsumerMock();
    }

    @Override
    public PersonSelftestConsumer personSelftestConsumer() {
        return Mockito.mock(PersonSelftestConsumer.class);
    }
}
