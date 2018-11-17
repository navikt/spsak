package no.nav.foreldrepenger.web.app.tjenester.fagsak;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.ProsessTaskGruppeIdDto;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.app.FagsakApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.app.FagsakSamlingForBruker;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.FagsakDto;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.PersonDto;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SaksnummerDto;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SokefeltDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

@Api(tags = { "fagsak" })
@Path("/fagsak")
@RequestScoped
@Transaction
public class FagsakRestTjeneste {

    private FagsakApplikasjonTjeneste fagsakApplikasjonTjeneste;

    private RevurderingTjenesteProvider fagsakRevurderingTjenesteProvider;

    public FagsakRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public FagsakRestTjeneste(FagsakApplikasjonTjeneste fagsakApplikasjonTjeneste, RevurderingTjenesteProvider fagsakRevurderingTjenesteProvider) {
        this.fagsakApplikasjonTjeneste = fagsakApplikasjonTjeneste;
        this.fagsakRevurderingTjenesteProvider = fagsakRevurderingTjenesteProvider;
    }

    @GET
    @Path("/status")
    @Timed
    @ApiOperation(value = "Url for å polle på fagsak mens behandlingprosessen pågår i bakgrunnen(asynkront)", notes = ("Returnerer link til enten samme (hvis ikke ferdig) eller redirecter til /fagsak dersom asynkrone operasjoner er ferdig."))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returnerer Status", response = AsyncPollingStatus.class),
            @ApiResponse(code = 418, message = "ProsessTasks har feilet", response = AsyncPollingStatus.class, responseHeaders = {
                    @ResponseHeader(name = "Location") }),
            @ApiResponse(code = 303, message = "Pågående prosesstasks avsluttet", responseHeaders = { @ResponseHeader(name = "Location") })
    })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentFagsakMidlertidigStatus(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto idDto,
                                                @Nullable @QueryParam("gruppe") @Valid ProsessTaskGruppeIdDto gruppeDto)
            throws URISyntaxException {
        Saksnummer saksnummer = new Saksnummer(idDto.getVerdi());
        String gruppe = gruppeDto == null ? null : gruppeDto.getGruppe();
        Optional<AsyncPollingStatus> prosessTaskGruppePågår = fagsakApplikasjonTjeneste.sjekkProsessTaskPågår(saksnummer, gruppe);
        return Redirect.tilFagsakEllerPollStatus(saksnummer, prosessTaskGruppePågår.orElse(null));
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Hent fagsak for saksnummer", notes = ("Returnerer fagsak for gitt saksnummer."))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returnerer fagsak", response = FagsakDto.class),
            @ApiResponse(code = 404, message = "Fagsak ikke tilgjengelig") })
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentFagsak(@NotNull @QueryParam("saksnummer") @Valid SaksnummerDto s) {

        Saksnummer saksnummer = new Saksnummer(s.getVerdi());
        FagsakSamlingForBruker view = fagsakApplikasjonTjeneste.hentFagsakForSaksnummer(saksnummer);
        List<FagsakDto> list = tilDtoer(view);
        if (list.isEmpty()) {
            // return 403 Forbidden istdf 404 Not Found (sikkerhet - ikke avslør for mye)
            return Response.status(Response.Status.FORBIDDEN).build();
        } else if (list.size() == 1) {
            return Response.ok(list.get(0)).build();
        } else {
            throw new IllegalStateException(
                "Utvikler-feil: fant mer enn en fagsak for saksnummer [" + saksnummer + "], skal ikke være mulig: fant " + list.size());
        }
    }

    @POST
    @Timed
    @Path("/sok")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Søk etter saker på saksnummer eller fødselsnummer", notes = ("Spesifikke saker kan søkes via saksnummer. " +
        "Oversikt over saker knyttet til en bruker kan søkes via fødselsnummer eller d-nummer."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<FagsakDto> søkFagsaker(@ApiParam("Søkestreng kan være saksnummer, fødselsnummer eller D-nummer.") @Valid SokefeltDto søkestreng) {
        FagsakSamlingForBruker view = fagsakApplikasjonTjeneste.hentSaker(søkestreng.getSearchString());
        return tilDtoer(view);
    }

    private List<FagsakDto> tilDtoer(FagsakSamlingForBruker view) {
        if (view.isEmpty()) {
            return new ArrayList<>();
        }
        Personinfo brukerInfo = view.getBrukerInfo();

        PersonDto personDto = new PersonDto(brukerInfo.getNavn(), brukerInfo.getAlder(), String.valueOf(brukerInfo.getPersonIdent().getIdent()),
            brukerInfo.erKvinne(), brukerInfo.getPersonstatus(), brukerInfo.getDiskresjonskode(), brukerInfo.getDødsdato());

        List<FagsakDto> dtoer = new ArrayList<>();
        List<FagsakSamlingForBruker.FagsakRad> fagsakInfoer = view.getFagsakInfoer();
        for (FagsakSamlingForBruker.FagsakRad info : fagsakInfoer) {
            Fagsak fagsak = info.getFagsak();
            Boolean kanRevurderingOpprettes = fagsakRevurderingTjenesteProvider.finnRevurderingTjenesteFor(fagsak).kanRevurderingOpprettes(fagsak);
            LocalDate fødselsdato = info.getFødselsdato();
            dtoer.add(new FagsakDto(fagsak, personDto, kanRevurderingOpprettes, fagsak.getSkalTilInfotrygd()));
        }
        return dtoer;
    }

}
