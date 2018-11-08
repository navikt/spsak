package no.nav.vedtak.felles.integrasjon.organisasjon;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;

public class OrganisasjonConsumerImpl implements OrganisasjonConsumer {
    public static final String SERVICE_IDENTIFIER = "OrganisasjonV4";

    private OrganisasjonV4 port;

    public OrganisasjonConsumerImpl(OrganisasjonV4 port) {
        this.port = port;
    }

    @Override
    public HentOrganisasjonResponse hentOrganisasjon(HentOrganisasjonRequest request) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        try {
            return port.hentOrganisasjon(convertToWSRequest(request));
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    private no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest convertToWSRequest(HentOrganisasjonRequest request) {
        no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest result = new no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest();
        result.setOrgnummer(request.getOrgnummer());
        return result;
    }
}
