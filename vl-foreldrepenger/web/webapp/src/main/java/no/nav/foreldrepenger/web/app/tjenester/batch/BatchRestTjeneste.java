package no.nav.foreldrepenger.web.app.tjenester.batch;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.BATCH;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import javax.enterprise.context.ApplicationScoped;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.batch.BatchArguments;
import no.nav.foreldrepenger.batch.BatchSupportTjeneste;
import no.nav.foreldrepenger.batch.BatchTjeneste;
import no.nav.foreldrepenger.batch.feil.BatchFeil;
import no.nav.foreldrepenger.web.app.tjenester.batch.args.BatchArgumentsDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "batch")
@Path("/batch")
@ApplicationScoped
@Transaction
public class BatchRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(BatchRestTjeneste.class);

    private BatchSupportTjeneste batchSupportTjeneste;

    public BatchRestTjeneste() {
        // For CDI
    }

    @Inject
    public BatchRestTjeneste(BatchSupportTjeneste batchSupportTjeneste) {
        this.batchSupportTjeneste = batchSupportTjeneste;
    }

    private static String retrieveBatchServiceFrom(String executionId) {
        if (executionId != null && executionId.contains("-")) {
            return executionId.substring(0, executionId.indexOf('-'));
        }
        throw new IllegalStateException();
    }

    /**
     * Kalles på for å logge brukeren inn i løsningen. Dette for å ha minimalt med innloggingslogikk i bash-scriptet
     *
     * @return alltid 200 - OK
     */
    @GET
    @Path("/init")
    @BeskyttetRessurs(action = READ, ressurs = DRIFT, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response init() {
        return Response.ok().build();
    }

    @POST
    @Path("/launch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Start batchjob")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Starter batch og returnerer executionId", response = String.class),
        @ApiResponse(code = 400, message = "Ukjent batch forespurt."),
        @ApiResponse(code = 500, message = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = CREATE, ressurs = BATCH)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response startBatch(@NotNull @QueryParam("batchName") @Valid BatchNameDto batchName, @Valid BatchArgumentsDto args) {
        String name = batchName.getVerdi();
        final BatchTjeneste batchTjeneste = batchSupportTjeneste.finnBatchTjenesteForNavn(name);
        if (batchTjeneste != null) {
            final BatchArguments arguments = batchTjeneste.createArguments(args.getArguments());
            if (arguments.isValid()) {
                logger.info("Starter batch {}", LoggerUtils.removeLineBreaks(name)); // NOSONAR
                return Response.ok(batchTjeneste.launch(arguments)).build();
            } else {
                throw BatchFeil.FACTORY.ugyldigeJobParametere(arguments).toException();
            }
        }
        BatchFeil.FACTORY.ugyldiJobbNavnOppgitt(name).log(logger);
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/poll")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Poll status of batchjob")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Henter ut exitkode for executionId", response = String.class),
        @ApiResponse(code = 400, message = "Ukjent batch forespurt."),
        @ApiResponse(code = 500, message = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = READ, ressurs = DRIFT, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response poll(@NotNull @QueryParam("executionId") @Valid BatchExecutionDto dto) {
        final String batchName = retrieveBatchServiceFrom(dto.getExecutionId());
        final BatchTjeneste batchTjeneste = batchSupportTjeneste.finnBatchTjenesteForNavn(batchName);
        if (batchTjeneste != null) {
            return Response.ok(batchTjeneste.status(dto.getExecutionId()).value()).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }


    @POST
    @Path("/autorun")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Start task for å kjøre batchjobs")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Starter batch-scheduler."),
        @ApiResponse(code = 500, message = "Feilet pga ukjent feil.")
    })
    @BeskyttetRessurs(action = CREATE, ressurs = BATCH)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response autoRunBatch() {
        batchSupportTjeneste.startBatchSchedulerTask();
        return Response.ok().build();
    }

}
