package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import java.time.LocalDate;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumerProducer;

@Alternative
@Priority(1)
@Dependent
public class OrganisasjonConsumerProducerMock extends OrganisasjonConsumerProducer {

    private static final String MOCK_ORG = "EPLEHUSET AS";

    @Override
    public OrganisasjonConsumer organisasjonConsumer() {
        class OrganisasjonConsumerMock implements OrganisasjonConsumer {

            @Override
            public HentOrganisasjonResponse hentOrganisasjon(no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest var1) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
                HentOrganisasjonResponse response = new HentOrganisasjonResponse();
                Organisasjon organisasjon = new Organisasjon();
                UstrukturertNavn ustrukturertNavn = new UstrukturertNavn();
                ustrukturertNavn.getNavnelinje().add(MOCK_ORG);
                organisasjon.setOrgnummer(var1.getOrgnummer());
                organisasjon.setNavn(ustrukturertNavn);
                organisasjon.setOrganisasjonDetaljer(lagOrganisasjonsDetaljer());
                response.setOrganisasjon(organisasjon);

                return response;
            }
        }
        return new OrganisasjonConsumerMock();
    }

    private OrganisasjonsDetaljer lagOrganisasjonsDetaljer() {
        OrganisasjonsDetaljer detaljer = new OrganisasjonsDetaljer();

        try {
            XMLGregorianCalendar regDato = DateUtil.convertToXMLGregorianCalendar(LocalDate.now().minusMonths(1));
            detaljer.setRegistreringsDato(regDato);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return detaljer;
    }

}
