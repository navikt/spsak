package no.nav.foreldrepenger.web.app.tjenester.forsendelse;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.domene.mottak.forsendelse.ForsendelseIdDto;
import no.nav.foreldrepenger.domene.mottak.forsendelse.ForsendelseStatusDataDTO;
import no.nav.foreldrepenger.domene.mottak.forsendelse.tjeneste.ForsendelseStatusTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "dokumentforsendelse")
@Path("/dokumentforsendelse")
@RequestScoped
@Transaction
public class ForsendelseStatusRestTjeneste {

    private ForsendelseStatusTjeneste forsendelseStatusTjeneste;

    public ForsendelseStatusRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public ForsendelseStatusRestTjeneste(ForsendelseStatusTjeneste forsendelseStatusTjeneste) {
        this.forsendelseStatusTjeneste = forsendelseStatusTjeneste;
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @ApiOperation(value = "Søker om status på prossesseringen av et mottatt dokument",
        notes = ("TODO:"))
    @ApiResponses(value = {
        @ApiResponse(
            code = 200,
            message = "Status og Periode",
            response = ForsendelseStatusDataDTO.class
        ),
    })
    public ForsendelseStatusDataDTO getStatusInformasjon(@NotNull @QueryParam("forsendelseId") @ApiParam("forsendelseId") @Valid ForsendelseIdDto forsendelseIdDto) {
        return forsendelseStatusTjeneste.getStatusInformasjon(forsendelseIdDto);
    }
}
