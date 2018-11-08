package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Enhetsstatus;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeResponse;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient.ArbeidsfordelingConsumer;

@Dependent
@Alternative
@Priority(1)
public class ArbeidsfordelingConsumerMock implements ArbeidsfordelingConsumer {

    private static final String RESPONSE_ENHETS_ID = "1234";
    private static final String VIKEN_ENHETS_ID = "2103";

    @Override
    public FinnBehandlendeEnhetListeResponse finnBehandlendeEnhetListe(FinnBehandlendeEnhetListeRequest finnBehandlendeEnhetListeRequest) throws FinnBehandlendeEnhetListeUgyldigInput {
        return opprettResponseMedEnEnhet();
    }

    @Override
    public FinnAlleBehandlendeEnheterListeResponse finnAlleBehandlendeEnheterListe(FinnAlleBehandlendeEnheterListeRequest finnAlleBehandlendeEnheterListeRequest) throws FinnAlleBehandlendeEnheterListeUgyldigInput {
        return opprettResponseForHentAlleEnheterListe();
    }

    private FinnBehandlendeEnhetListeResponse opprettResponseMedEnEnhet()  {
        FinnBehandlendeEnhetListeResponse response = new FinnBehandlendeEnhetListeResponse();
        Organisasjonsenhet enhet = new Organisasjonsenhet();
        enhet.setEnhetId(RESPONSE_ENHETS_ID);
        enhet.setEnhetNavn("test navn");
        enhet.setOrganisasjonsnummer("45678");

        response.getBehandlendeEnhetListe().add(enhet);
        return response;
    }

    private FinnAlleBehandlendeEnheterListeResponse opprettResponseForHentAlleEnheterListe() {
        FinnAlleBehandlendeEnheterListeResponse response = new FinnAlleBehandlendeEnheterListeResponse();
        Organisasjonsenhet enhet1 = new Organisasjonsenhet();
        enhet1.setEnhetId(RESPONSE_ENHETS_ID);
        enhet1.setEnhetNavn("Anne Lier");
        enhet1.setOrganisasjonsnummer("5443");
        enhet1.setStatus(Enhetsstatus.AKTIV);
        response.getBehandlendeEnhetListe().add(enhet1);
        Organisasjonsenhet enhet2 = new Organisasjonsenhet();
        enhet2.setEnhetId(VIKEN_ENHETS_ID);
        enhet2.setEnhetNavn("NAV Viken");
        enhet2.setOrganisasjonsnummer("5443");
        enhet2.setStatus(Enhetsstatus.AKTIV);
        response.getBehandlendeEnhetListe().add(enhet2);

        return response;
    }
}
