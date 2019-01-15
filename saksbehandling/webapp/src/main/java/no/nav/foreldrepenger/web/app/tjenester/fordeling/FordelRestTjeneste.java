package no.nav.foreldrepenger.web.app.tjenester.fordeling;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.SaksbehandlingDokumentmottakTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.fordeling.sak.OpprettSakOrchestrator;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

/**
 * Mottar dokumenter fra f.eks. FPFORDEL og håndterer dispatch internt for saksbehandlingsløsningen.
 */
@Api(tags = {"fordel"})
@Path("/fordel")
@RequestScoped
@Transaction
public class FordelRestTjeneste {
    // FIXME (u139158): PK-44270 Må versjonere tjenestene og DTO'ene

    private static final String JSON_UTF8 = "application/json; charset=UTF-8";

    private SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste;
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private KodeverkRepository kodeverkRepository;
    private BehandlingRepository behandlingRepository;
    private OpprettSakOrchestrator opprettSakOrchestrator;

    public FordelRestTjeneste() {// For Rest-CDI
    }

    @Inject
    public FordelRestTjeneste(SaksbehandlingDokumentmottakTjeneste dokumentmottakTjeneste,
                              DokumentArkivTjeneste dokumentArkivTjeneste,
                              FagsakTjeneste fagsakTjeneste,
                              GrunnlagRepositoryProvider repositoryProvider,
                              OpprettSakOrchestrator opprettSakOrchestrator) {
        this.dokumentmottakTjeneste = dokumentmottakTjeneste;
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.opprettSakOrchestrator = opprettSakOrchestrator;
    }

    @POST
    @Path("/vurderFagsystem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Informasjon om en fagsak"// , notes = ("Varsel om en ny journalpost som skal behandles i systemet.")
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public BehandlendeFagsystemDto vurderFagsystem(@ApiParam("Krever behandlingstemaOffisiellKode") @Valid VurderFagsystemDto vurderFagsystemDto) {
        // FIXME SP: vurderFagsystem. Uklart om trengs?
        return null;
    }

    @POST
    @Path("/fagsak/informasjon")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Informasjon om en fagsak"// , notes = ("Varsel om en ny journalpost som skal behandles i systemet.")
    )
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public FagsakInfomasjonDto fagsak(@ApiParam("Saksnummeret det skal hentes saksinformasjon om") @Valid SaksnummerDto saksnummerDto) {
        Optional<Fagsak> optFagsak = fagsakTjeneste.finnFagsakGittSaksnummer(new Saksnummer(saksnummerDto.getSaksnummer()), false);
        if (!optFagsak.isPresent()) {
            // FIXME (u139158): PK- hvordan skal dette håndteres?
            // throw BehandleDokumentServiceFeil.FACTORY.finnerIkkeFagsak(removeLineBreaks(saksnummer.toString())).toException();
            return null;
        }
        behandlingRepository.hentSisteBehandlingForFagsakId(optFagsak.get().getId());
        AktørId aktørId = optFagsak.get().getAktørId();
        return new FagsakInfomasjonDto(aktørId.getId());
    }

    @POST
    @Path("/fagsak/opprett")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Ny journalpost skal behandles.", notes = ("Varsel om en ny journalpost som skal behandles i systemet."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public SaksnummerDto opprettSak(@ApiParam("Oppretter fagsak") @Valid OpprettSakDto opprettSakDto) {
        Optional<String> journalpostId = opprettSakDto.getJournalpostId();

        AktørId aktørId = new AktørId(opprettSakDto.getAktørId());

        Saksnummer s;
        if (journalpostId.isPresent()) {
            s = opprettSakOrchestrator.opprettSak(new JournalpostId(journalpostId.get()), aktørId);
        } else {
            s = opprettSakOrchestrator.opprettSak(aktørId);
        }
        return new SaksnummerDto(s.getVerdi());
    }

    @POST
    @Path("/fagsak/knyttJournalpost")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Knytt journalpost til fagsak.", notes = ("Før en journalpost journalføres på en fagsak skal fagsaken oppdateres med journalposten."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public void knyttSakOgJournalpost(@ApiParam("Saksnummer og JournalpostId som skal knyttes sammen") @Valid JournalpostKnyttningDto journalpostKnytningDto) {
        opprettSakOrchestrator.knyttSakOgJournalpost(new Saksnummer(journalpostKnytningDto.getSaksnummer()), new JournalpostId(journalpostKnytningDto.getJournalpostId()));
    }

}
