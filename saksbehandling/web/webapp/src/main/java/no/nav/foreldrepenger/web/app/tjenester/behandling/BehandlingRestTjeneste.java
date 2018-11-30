package no.nav.foreldrepenger.web.app.tjenester.behandling;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.ByttBehandlendeEnhetDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.NyBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.ReåpneBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.SettBehandlingPaVentDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.ProsessTaskGruppeIdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.UtvidetBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SaksnummerDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.TomtResultatException;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

@Api(tags = { "behandlinger" })
@Path("/behandlinger")
@RequestScoped
@Transaction
public class BehandlingRestTjeneste {

    private interface BehandlingRestTjenesteFeil extends DeklarerteFeil {
        BehandlingRestTjenesteFeil FACTORY = FeilFactory.create(BehandlingRestTjenesteFeil.class); // NOSONAR

        @TekniskFeil(feilkode = "FP-760410", feilmelding = "Fant ikke fagsak med saksnummer %s", logLevel = ERROR, exceptionClass = TomtResultatException.class)
        Feil fantIkkeFagsak(Saksnummer saksnummer);
    }

    private BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste;
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private KodeverkRepository kodeverkRepository;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingDtoTjeneste behandlingDtoTjeneste;

    public BehandlingRestTjeneste() {
        // for resteasy
    }

    @Inject
    public BehandlingRestTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                  BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste,
                                  BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste,
                                  FagsakTjeneste fagsakTjeneste,
                                  HenleggBehandlingTjeneste henleggBehandlingTjeneste,
                                  BehandlingDtoTjeneste behandlingDtoTjeneste) {

        this.behandlingutredningTjeneste = behandlingsutredningApplikasjonTjeneste;
        this.behandlingsprosessTjeneste = behandlingsprosessTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
        this.behandlingDtoTjeneste = behandlingDtoTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Init hent behandling")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Hent behandling initiert, Returnerer link til å polle på fremdrift", responseHeaders = {
                    @ResponseHeader(name = "Location") }),
            @ApiResponse(code = 303, message = "Behandling tilgjenglig (prosesstasks avsluttet)", responseHeaders = { @ResponseHeader(name = "Location") })
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandling(@NotNull @Valid BehandlingIdDto idDto) throws URISyntaxException {
        Long behandlingId = idDto.getBehandlingId();
        Behandling behandling = behandlingsprosessTjeneste.hentBehandling(behandlingId);

        Optional<String> gruppeOpt = behandlingsprosessTjeneste.sjekkOgForberedAsynkInnhentingAvRegisteropplysningerOgKjørProsess(behandling);
        
        // sender alltid til poll status slik at vi får sjekket på utestående prosess tasks også.
        return Redirect.tilBehandlingPollStatus(behandlingId, gruppeOpt);

    }

    @GET
    @Path("/status")
    @ApiOperation(value = "Url for å polle på behandling mens behandlingprosessen pågår i bakgrunnen(asynkront)", notes = ("Returnerer link til enten samme (hvis ikke ferdig) eller redirecter til /behandlinger dersom asynkrone operasjoner er ferdig."))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returnerer Status", response = AsyncPollingStatus.class),
            @ApiResponse(code = 418, message = "ProsessTasks har feilet", response = AsyncPollingStatus.class, responseHeaders = {
                    @ResponseHeader(name = "Location") }),
            @ApiResponse(code = 303, message = "Behandling tilgjenglig (prosesstasks avsluttet)", responseHeaders = { @ResponseHeader(name = "Location") })
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingMidlertidigStatus(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto idDto,
                                                    @Nullable @QueryParam("gruppe") @Valid ProsessTaskGruppeIdDto gruppeDto)
            throws URISyntaxException {
        Long behandlingId = idDto.getBehandlingId();
        String gruppe = gruppeDto == null ? null : gruppeDto.getGruppe();
        Behandling behandling = behandlingsprosessTjeneste.hentBehandling(behandlingId);
        Optional<AsyncPollingStatus> prosessTaskGruppePågår = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return Redirect.tilBehandlingEllerPollStatus(behandlingId, prosessTaskGruppePågår.orElse(null));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Hent behandling gitt id", notes = ("Returnerer behandlingen som er tilknyttet id. Dette er resultat etter at asynkrone operasjoner er utført."))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returnerer Behandling", response = UtvidetBehandlingDto.class),
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentBehandlingResultat(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto idDto) {

        Long behandlingId = idDto.getBehandlingId();
        Behandling behandling = behandlingsprosessTjeneste.hentBehandling(behandlingId);

        AsyncPollingStatus taskStatus = behandlingsprosessTjeneste.sjekkProsessTaskPågårForBehandling(behandling, null).orElse(null);

        UtvidetBehandlingDto dto = behandlingDtoTjeneste.lagUtvidetBehandlingDto(behandling, taskStatus);

        ResponseBuilder responseBuilder = Response.ok().entity(dto);

        return responseBuilder.build();
    }

    @POST
    @Path("/sett-pa-vent")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Setter behandling på vent")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void settBehandlingPaVent(@ApiParam("Frist for behandling på vent") @Valid SettBehandlingPaVentDto dto) {
        behandlingutredningTjeneste.kanEndreBehandling(dto.getBehandlingId(), dto.getBehandlingVersjon());
        behandlingutredningTjeneste.settBehandlingPaVent(dto.getBehandlingId(), dto.getFrist(), dto.getVentearsak());
    }

    @POST
    @Path("/endre-pa-vent")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Endrer ventefrist for behandling på vent")
    @BeskyttetRessurs(action = UPDATE, ressurs = BeskyttetRessursResourceAttributt.VENTEFRIST)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void endreFristForBehandlingPaVent(
                                              @ApiParam("Frist for behandling på vent") @Valid SettBehandlingPaVentDto dto) {
        behandlingutredningTjeneste.kanEndreBehandling(dto.getBehandlingId(), dto.getBehandlingVersjon());
        behandlingutredningTjeneste.endreBehandlingPaVent(dto.getBehandlingId(), dto.getFrist(), dto.getVentearsak());
    }

    @POST
    @Path("/henlegg")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Henlegger behandling")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void henleggBehandling(@ApiParam("Henleggelsesårsak") @Valid HenleggBehandlingDto dto) {
        Long behandlingId = dto.getBehandlingId();
        behandlingutredningTjeneste.kanEndreBehandling(behandlingId, dto.getBehandlingVersjon());
        BehandlingResultatType årsakKode = tilHenleggBehandlingResultatType(dto.getÅrsakKode());
        henleggBehandlingTjeneste.henleggBehandling(behandlingId, årsakKode, dto.getBegrunnelse());
    }

    @POST
    @Path("/gjenoppta")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gjenopptar behandling som er satt på vent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Gjenoppta behandling påstartet i bakgrunnen", responseHeaders = { @ResponseHeader(name = "Location") }),
    })
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response gjenopptaBehandling(
                                        @ApiParam("BehandlingId for behandling som skal gjenopptas") @Valid GjenopptaBehandlingDto dto)
            throws URISyntaxException {
        Long behandlingId = dto.getBehandlingId();
        Long behandlingVersjon = dto.getBehandlingVersjon();

        // precondition - sjekk behandling versjon/lås
        behandlingutredningTjeneste.kanEndreBehandling(behandlingId, behandlingVersjon);

        // gjenoppta behandling ( sparkes i gang asynkront, derav redirect til status url under )
        Optional<String> gruppeOpt = behandlingsprosessTjeneste.gjenopptaBehandling(behandlingsprosessTjeneste.hentBehandling(behandlingId));

        return Redirect.tilBehandlingPollStatus(behandlingId, gruppeOpt);
    }

    @POST
    @Path("/bytt-enhet")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Bytte behandlende enhet")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void byttBehandlendeEnhet(@ApiParam("Ny enhet som skal byttes") @Valid ByttBehandlendeEnhetDto dto) {
        Long behandlingId = dto.getBehandlingId();
        Long behandlingVersjon = dto.getBehandlingVersjon();
        behandlingutredningTjeneste.kanEndreBehandling(behandlingId, behandlingVersjon);

        String enhetId = dto.getEnhetId();
        String enhetNavn = dto.getEnhetNavn();
        String begrunnelse = dto.getBegrunnelse();
        behandlingutredningTjeneste.byttBehandlendeEnhet(behandlingId, new OrganisasjonsEnhet(enhetId, enhetNavn), begrunnelse, HistorikkAktør.SAKSBEHANDLER);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Opprette ny behandling")
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Opprett ny behandling pågår", responseHeaders = { @ResponseHeader(name = "Location") }),
    })
    @BeskyttetRessurs(action = CREATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response opprettNyBehandling(@ApiParam("Saksnummer og parametre for ny behandling") @Valid NyBehandlingDto dto)
            throws URISyntaxException {
        Saksnummer saksnummer = new Saksnummer(Long.toString(dto.getSaksnummer()));
        Optional<Fagsak> funnetFagsak = fagsakTjeneste.finnFagsakGittSaksnummer(saksnummer, true);
        String kode = dto.getBehandlingType().getKode();

        if (!funnetFagsak.isPresent()) {
            throw BehandlingRestTjenesteFeil.FACTORY.fantIkkeFagsak(saksnummer).toException();
        }

        Fagsak fagsak = funnetFagsak.get();

        if (BehandlingType.REVURDERING.getKode().equals(kode)) {
            BehandlingÅrsakType behandlingÅrsakType = kodeverkRepository.finn(BehandlingÅrsakType.class, dto.getBehandlingArsakType().getKode());
            Behandling behandling = behandlingutredningTjeneste.opprettRevurdering(fagsak, behandlingÅrsakType);
            String gruppe = behandlingsprosessTjeneste.asynkFortsettBehandlingsprosess(behandling);
            return Redirect.tilBehandlingPollStatus(behandling.getId(), Optional.of(gruppe));

        } else if (BehandlingType.FØRSTEGANGSSØKNAD.getKode().equals(kode)) {
            behandlingutredningTjeneste.opprettNyFørstegangsbehandling(fagsak.getId(), saksnummer);
            // ved førstegangssønad opprettes egen task for vurdere denne,
            // sender derfor ikke viderer til prosesser behandling (i motsetning til de andre).
            // må også oppfriske hele sakskomplekset, så sender til fagsak poll url
            return Redirect.tilFagsakPollStatus(fagsak.getSaksnummer(), Optional.empty());

        } else {
            throw new IllegalArgumentException("Støtter ikke opprette ny behandling for behandlingType:" + kode);
        }

    }

    private BehandlingResultatType tilHenleggBehandlingResultatType(String årsak) {
        return BehandlingResultatType.getAlleHenleggelseskoder().stream().filter(k -> k.getKode().equals(årsak))
            .findFirst().orElse(null);
    }

    @GET
    @Path("/alle")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Søk etter behandlinger på saksnummer", notes = ("Returnerer alle behandlinger som er tilknyttet saksnummer."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BehandlingDto> hentBehandlinger(
                                                @NotNull @QueryParam("saksnummer") @ApiParam("Saksnummer må være et eksisterende saksnummer") @Valid SaksnummerDto s) {
        Saksnummer saksnummer = new Saksnummer(s.getVerdi());
        List<Behandling> behandlinger = behandlingutredningTjeneste.hentBehandlingerForSaksnummer(saksnummer);
        return behandlingDtoTjeneste.lagBehandlingDtoer(behandlinger);
    }

    @POST
    @Path("/opne-for-endringer")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Åpner behandling for endringer")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Åpning av behandling for endringer påstartet i bakgrunnen", responseHeaders = { @ResponseHeader(name = "Location") }),
    })
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response åpneBehandlingForEndringer(@ApiParam("BehandlingId for behandling som skal åpnes for endringer") @Valid ReåpneBehandlingDto dto) throws URISyntaxException {
        Long behandlingId = dto.getBehandlingId();
        Long behandlingVersjon = dto.getBehandlingVersjon();

        // precondition - sjekk behandling versjon/lås
        behandlingutredningTjeneste.kanEndreBehandling(behandlingId, behandlingVersjon);

        behandlingsprosessTjeneste.asynkTilbakestillOgÅpneBehandlingForEndringer(behandlingId);

        return Redirect.tilBehandlingPollStatus(behandlingId, Optional.empty());
    }


}
