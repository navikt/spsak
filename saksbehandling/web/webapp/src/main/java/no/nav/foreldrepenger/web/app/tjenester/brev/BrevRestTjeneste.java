package no.nav.foreldrepenger.web.app.tjenester.brev;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "brev")
@Path("/brev")
@ApplicationScoped
@Transaction
public class BrevRestTjeneste {


    public BrevRestTjeneste() {
        // For Rest-CDI
    }

    @POST
    @Path("/forhandsvis")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer en pdf som er en forhåndsvisning av brevet")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentForhåndsvisningDokument(@Valid BehandlingIdDto dto) { // NOSONAR
        return Response.noContent().build();
    }

    @POST
    @Path("/bestill")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Bestiller generering og sending av brevet")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public void bestillDokument(@Valid BehandlingIdDto dto) { // NOSONAR
    }

    @POST
    @Path("/mottakere")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Henter liste med tilgjengelige mottakere av melding.")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<String> hentMottakere(@Valid BehandlingIdDto dto) {
        return List.of(); // NOSONAR
    }

    @POST

    @Path("/maler")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Henter liste over tilgjengelige brevtyper")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Object> hentMaler(@Valid BehandlingIdDto dto) {
        return List.of(); // NOSONAR
    }

    @POST

    @Path("/varsel/revurdering")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Sjekk har varsel sendt om revurdering")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Boolean harSendtVarselOmRevurdering(@Valid BehandlingIdDto dto) {
        return false; // NOSONAR
    }
}
