package no.nav.foreldrepenger.sak.v1;

import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.binding.ForeldrepengesakV1;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.informasjon.Saksstatus;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.meldinger.FinnSakListeRequest;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

/**
 * Webservice for å finne relevante fagsaker i VL.
 */

@Dependent
@WebService(wsdlLocation = "wsdl/no/nav/tjeneste/virksomhet/foreldrepengesak/v1/foreldrepengesak.wsdl", serviceName = "foreldrepengesak_v1", portName = "foreldrepengesak_v1Port", endpointInterface = "no.nav.tjeneste.virksomhet.foreldrepengesak.v1.binding.ForeldrepengesakV1")
@SoapWebService(endpoint = "/sak/finnSak/v1", tjenesteBeskrivelseURL = "https://confluence.adeo.no/pages/viewpage.action?pageId=220528950")
public class FinnSakService implements ForeldrepengesakV1 {

    public static final Logger logger = LoggerFactory.getLogger(FinnSakService.class);

    private FagsakRepository fagsakRepository;
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepository behandlingRepository;

    public FinnSakService() {
        // NOSONAR: for CDI
    }

    @Inject
    public FinnSakService(BehandlingRepositoryProvider repositoryProvider) {
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Override
    public void ping() {
        logger.debug("ping");
    }

    @Override
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public FinnSakListeResponse finnSakListe(@TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) FinnSakListeRequest request)
            throws FinnSakListeSikkerhetsbegrensning {

        Aktoer sakspart = request.getSakspart();
        String aktørid = sakspart.getAktoerId();

        List<Fagsak> fagsaker = fagsakRepository.hentForBrukerAktørId(new AktørId(aktørid));

        try {
            return lagResponse(fagsaker);
        } catch (DatatypeConfigurationException e) {
            throw FinnSakServiceFeil.FACTORY.konverteringsfeil(e).toException();
        }
    }

    // pkg scope for enhetstest
    FinnSakListeResponse lagResponse(List<Fagsak> fagsaker) throws DatatypeConfigurationException {
        FinnSakListeResponse response = new FinnSakListeResponse();
        List<Sak> saksliste = response.getSakListe();
        for (Fagsak fagsak : fagsaker) {
            saksliste.add(lagEksternRepresentasjon(fagsak));
        }
        return response;
    }

    private Sak lagEksternRepresentasjon(Fagsak fagsak) throws DatatypeConfigurationException {
        Sak sak = new Sak();
        FagsakStatus status = fagsak.getStatus();
        sak.setStatus(lagEksternRepresentasjon(status));
        sak.setBehandlingstema(lagEksternRepresentasjonBehandlingstema(fagsak));
        sak.setSakId(fagsak.getSaksnummer().getVerdi());
        sak.setEndret(DateUtil.convertToXMLGregorianCalendar(fagsak.getEndretTidspunkt()));
        sak.setOpprettet(DateUtil.convertToXMLGregorianCalendar(fagsak.getOpprettetTidspunkt()));
        return sak;
    }

    private Behandlingstema lagEksternRepresentasjonBehandlingstema(Fagsak fagsak) {
        BehandlingTema behandlingTemaKodeliste = kodeverkRepository.finn(BehandlingTema.class, getBehandlingTema(fagsak));

        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue(behandlingTemaKodeliste.getOffisiellKode());
        behandlingstema.setTermnavn(behandlingTemaKodeliste.getNavn());
        return behandlingstema;
    }

    private Saksstatus lagEksternRepresentasjon(FagsakStatus status) {
        Saksstatus saksstatus = new Saksstatus();
        saksstatus.setValue(status.getKode()); // TODO (HUMLE): PK-41816 Endre når det bli klart hvilken kodemapping som gjelder
        saksstatus.setTermnavn(status.getNavn());
        return saksstatus;
    }

    private BehandlingTema getBehandlingTema(Fagsak fagsak) {
        if (!erStøttetYtelseType(fagsak.getYtelseType())) {
            throw FinnSakServiceFeil.FACTORY.ikkeStøttetYtelsestype(fagsak.getYtelseType()).toException();
        }

        BehandlingTema behandlingTema = getBehandlingsTemaForFagsak(fagsak);
        if (BehandlingTema.gjelderEngangsstønad(behandlingTema) || BehandlingTema.gjelderForeldrepenger(behandlingTema)) {
            return behandlingTema;
        } else {
            // det er riktig å rapportere på årsakstype, selv om koden over bruker BehandlingTema
            throw FinnSakServiceFeil.FACTORY.ikkeStøttetÅrsakstype(behandlingTema).toException();
        }
    }

    private BehandlingTema getBehandlingsTemaForFagsak(Fagsak s) {
        return BehandlingTema.fraFagsak(s);
    }

    private boolean erStøttetYtelseType(FagsakYtelseType fagsakYtelseType) {
        return FagsakYtelseType.FORELDREPENGER.equals(fagsakYtelseType);
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            FinnSakListeRequest req = (FinnSakListeRequest) obj;
            return AbacDataAttributter.opprett().leggTilAktørId(req.getSakspart().getAktoerId());
        }
    }

}
