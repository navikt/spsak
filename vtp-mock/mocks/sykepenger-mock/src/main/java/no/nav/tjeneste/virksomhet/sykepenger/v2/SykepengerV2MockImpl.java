package no.nav.tjeneste.virksomhet.sykepenger.v2;


import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.Addressing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fpmock2.testmodell.repo.TestscenarioBuilderRepository;
import no.nav.tjeneste.virksomhet.sykepenger.v2.binding.HentSykepengerListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sykepenger.v2.binding.SykepengerV2;
import no.nav.tjeneste.virksomhet.sykepenger.v2.meldinger.HentSykepengerListeRequest;
import no.nav.tjeneste.virksomhet.sykepenger.v2.meldinger.HentSykepengerListeResponse;

@Addressing
@WebService(name = "Sykepenger_v2", targetNamespace = "http://nav.no/tjeneste/virksomhet/sykepenger/v2/Sykepenger_v2")
@HandlerChain(file="Handler-chain.xml")
public class SykepengerV2MockImpl implements SykepengerV2 {

    private static final Logger LOG = LoggerFactory.getLogger(SykepengerV2MockImpl.class);

    private TestscenarioBuilderRepository scenarioRepository;

    public SykepengerV2MockImpl() {
    }

    public SykepengerV2MockImpl(TestscenarioBuilderRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    @WebMethod(action = "http://nav.no/tjeneste/virksomhet/sykepenger/v2/Sykepenger_v2/hentSykepengerListeRequest")
    @WebResult(name = "response", targetNamespace = "")
    @RequestWrapper(localName = "hentSykepengerListe", targetNamespace = "http://nav.no/tjeneste/virksomhet/sykepenger/v2", className = "no.nav.tjeneste.virksomhet.sykepenger.v2.HentSykepengerListe")
    @ResponseWrapper(localName = "hentSykepengerListeResponse", targetNamespace = "http://nav.no/tjeneste/virksomhet/sykepenger/v2", className = "no.nav.tjeneste.virksomhet.sykepenger.v2.HentSykepengerListeResponse")
    public HentSykepengerListeResponse hentSykepengerListe(
        @WebParam(name = "request", targetNamespace = "")
            HentSykepengerListeRequest request)
        throws HentSykepengerListeSikkerhetsbegrensning {

        LOG.info("request: ", request);
        HentSykepengerListeResponse ret = new HentSykepengerListeResponse();
        // TODO: populer ret.getSykmeldingsperiodeListe()
        return ret;
    }

    @WebMethod(action = "http://nav.no/tjeneste/virksomhet/sykepenger/v2/Sykepenger_v2/pingRequest")
    @RequestWrapper(localName = "ping", targetNamespace = "http://nav.no/tjeneste/virksomhet/sykepenger/v2", className = "no.nav.tjeneste.virksomhet.sykepenger.v2.Ping")
    @ResponseWrapper(localName = "pingResponse", targetNamespace = "http://nav.no/tjeneste/virksomhet/sykepenger/v2", className = "no.nav.tjeneste.virksomhet.sykepenger.v2.PingResponse")
    public void ping() {
            LOG.info("Ping mottatt og besvart");
    }
}
