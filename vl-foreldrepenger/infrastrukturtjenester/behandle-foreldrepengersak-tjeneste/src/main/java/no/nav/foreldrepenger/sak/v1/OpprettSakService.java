package no.nav.foreldrepenger.sak.v1;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakFeil;
import no.nav.foreldrepenger.sak.tjeneste.OpprettSakOrchestrator;
import no.nav.foreldrepenger.sak.tjeneste.UkjentPersonException;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.BehandleForeldrepengesakV1;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.OpprettSakSakEksistererAllerede;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.OpprettSakSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.OpprettSakUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.meldinger.OpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.meldinger.OpprettSakResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;
import no.nav.vedtak.felles.integrasjon.felles.ws.VLFaultListenerUnntakKonfigurasjon;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

/**
 * Webservice for å opprette sak i VL ved manuelle journalføringsoppgaver.
 */

/* @Transaction
 * HACK (u139158): Transaksjonsgrensen er for denne webservice'en flyttet til javatjenesten OpprettSakTjeneste
 * Dette er ikke i henhold til standard og kan ikke gjøres uten godkjenning fra sjefsarkitekt.
 * Grunnen for at det er gjort her er for å sikre at de tre kallene går i separate transaksjoner.
 * Se https://jira.adeo.no/browse/PKHUMLE-359 for detaljer.
 */
@Dependent
@WebService(
    wsdlLocation = "wsdl/no/nav/tjeneste/virksomhet/behandleForeldrepengesak/v1/behandleForeldrepengesak.wsdl",
    serviceName = "BehandleForeldrepengesak_v1",
    portName = "BehandleForeldrepengesak_v1Port",
    endpointInterface = "no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.BehandleForeldrepengesakV1")
@SoapWebService(endpoint = "/sak/opprettSak/v1", tjenesteBeskrivelseURL = "https://confluence.adeo.no/pages/viewpage.action?pageId=220529015")
public class OpprettSakService implements BehandleForeldrepengesakV1 {

    private static final Logger logger = LoggerFactory.getLogger(OpprettSakService.class);

    private OpprettSakOrchestrator opprettSakOrchestrator;
    private KodeverkRepository kodeverkRepository;
    private JournalTjeneste journalTjeneste;

    public OpprettSakService() {
        // NOSONAR: cdi
    }

    @Inject
    public OpprettSakService(OpprettSakOrchestrator opprettSakOrchestrator, KodeverkRepository kodeverkRepository,
                             JournalTjeneste journalTjeneste) {
        this.opprettSakOrchestrator = opprettSakOrchestrator;
        this.kodeverkRepository = kodeverkRepository;
        this.journalTjeneste = journalTjeneste;
    }

    @Override
    public void ping() {
        logger.debug("ping");
    }

    @Override
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public OpprettSakResponse opprettSak(
        @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) OpprettSakRequest opprettSakRequest)
        throws OpprettSakSakEksistererAllerede, OpprettSakSikkerhetsbegrensning, OpprettSakUgyldigInput {

        BehandlingTema behandlingTema = hentBehandlingstema(opprettSakRequest.getBehandlingstema().getValue());
        DokumentTypeId dokumentTypeId = validerJournalpostId(opprettSakRequest.getJournalpostId(), behandlingTema);
        JournalpostId journalpostId = new JournalpostId(opprettSakRequest.getJournalpostId());
        behandlingTema = validerBehandlingstema(behandlingTema, dokumentTypeId);
        // TODO: Gjennomgå logikk for andre dokumentTypeId'er som ikke kan gi grunnlag for å opprette saker - vedlegg mm.
        // Produksjonserfaring tilsier at det opprettes saker basert på vedlegg vi ikke kan starte behandling på
        // Gosys tviner valg av spesifikt behandlingtema og kan ha fått satt kategori - slik at vi kan avgjøre bedre.
        // Kan også bruke dokumentarkivtjeneste til å sjekke om dokument uten doktypeid har tittel som tilsier søknad
        // fpsak vil uansett prøve å raffinere dokumenttypeid udefinert før lagring i mottattdokument. Men der er ikke behTema tilgjengelig
        try {
            AktørId aktørId = validerAktørId(opprettSakRequest.getSakspart().getAktoerId());

            Saksnummer saksnummer = opprettSakOrchestrator.opprettSak(journalpostId, behandlingTema, aktørId);
            return lagResponse(saksnummer);
        } catch (UkjentPersonException e) {
            UgyldigInput faultInfo = lagUgyldigInput("AktørId", opprettSakRequest.getSakspart().getAktoerId());
            throw new OpprettSakUgyldigInput(faultInfo.getFeilmelding(), faultInfo, e);
        }
    }

    private DokumentTypeId validerJournalpostId(String journalpostId, BehandlingTema behandlingTema) throws OpprettSakUgyldigInput {
        final String feltnavnJournalpostId = "JournalpostId";
        if (!JournalpostId.erGyldig(journalpostId)) {
            UgyldigInput faultInfo = lagUgyldigInput(feltnavnJournalpostId, journalpostId);
            throw new OpprettSakUgyldigInput(faultInfo.getFeilmelding(), faultInfo);
        }
        // Hindre at man oppretter sak basert på en klage - de skal journalføres på eksisterende sak

        ArkivJournalPost arkivJournalPost = journalTjeneste.hentInngåendeJournalpostHoveddokument(new JournalpostId(journalpostId), DokumentTypeId.UDEFINERT);
        if (arkivJournalPost == null || arkivJournalPost.getHovedDokument() == null) {
            UgyldigInput faultInfo = lagUgyldigInput(feltnavnJournalpostId, journalpostId);
            throw new OpprettSakUgyldigInput(faultInfo.getFeilmelding(), faultInfo);
        }
        ArkivDokument dokument = arkivJournalPost.getHovedDokument();
        if (DokumentTypeId.KLAGE_DOKUMENT.equals(dokument.getDokumentTypeId()) || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokument.getDokumentKategori())) {
            UgyldigInput faultInfo = lagUgyldigInput(feltnavnJournalpostId, journalpostId);
            throw new OpprettSakUgyldigInput(faultInfo.getFeilmelding(), faultInfo);
        }
        if (DokumentTypeId.UDEFINERT.equals(dokument.getDokumentTypeId())) {
            if (DokumentKategori.SØKNAD.equals(dokument.getDokumentKategori()) && BehandlingTema.ENGANGSSTØNAD_FØDSEL.equals(behandlingTema)) {
                return DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
            }
            if (DokumentKategori.SØKNAD.equals(dokument.getDokumentKategori()) && BehandlingTema.ENGANGSSTØNAD_ADOPSJON.equals(behandlingTema)) {
                return DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON;
            }
        }
        return dokument.getDokumentTypeId();
    }

    private BehandlingTema hentBehandlingstema(String behandlingstemaOffisiellKode) throws OpprettSakUgyldigInput {
        BehandlingTema behandlingTema = null;
        if (behandlingstemaOffisiellKode != null) {
            behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, behandlingstemaOffisiellKode, BehandlingTema.UDEFINERT);
        }
        if (behandlingTema == null || BehandlingTema.UDEFINERT.equals(behandlingTema)) {
            UgyldigInput faultInfo = lagUgyldigInput("Behandlingstema", behandlingstemaOffisiellKode);
            throw new OpprettSakUgyldigInput(faultInfo.getFeilmelding(), faultInfo);
        }
        return behandlingTema;
    }

    private BehandlingTema validerBehandlingstema(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId)  {
        if (BehandlingTema.ENGANGSSTØNAD.equals(behandlingTema)) {
            if (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.equals(dokumentTypeId)) {
                return kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_FØDSEL);
            } else if (DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON.equals(dokumentTypeId)) {
                return kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
            }
        }
        return behandlingTema;
    }

    private AktørId validerAktørId(String aktørId) {
        if (aktørId == null) {
            throw OpprettSakFeil.FACTORY.finnerIkkePersonAktørIdNull().toException();
        }
        return new AktørId(aktørId);
    }

    private UgyldigInput lagUgyldigInput(String feltnavn, String value) {
        UgyldigInput faultInfo = new UgyldigInput();
        faultInfo.setFeilmelding(feltnavn + " med verdi " + value + " er ugyldig input");
        faultInfo.setFeilaarsak("Ugyldig input");
        return faultInfo;
    }

    private OpprettSakResponse lagResponse(Saksnummer saksnummer) {
        OpprettSakResponse response = new OpprettSakResponse();
        response.setSakId(saksnummer.getVerdi());
        return response;
    }

    @ApplicationScoped
    public static class Unntak extends VLFaultListenerUnntakKonfigurasjon {
        public Unntak() {
            super(OpprettSakUgyldigInput.class);
        }
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            OpprettSakRequest req = (OpprettSakRequest) obj;
            AbacDataAttributter dataAttributter = AbacDataAttributter.opprett();
            if (req.getSakspart() != null) {
                dataAttributter = dataAttributter.leggTilAktørId(req.getSakspart().getAktoerId());
            }
            return dataAttributter;
        }
    }
}
