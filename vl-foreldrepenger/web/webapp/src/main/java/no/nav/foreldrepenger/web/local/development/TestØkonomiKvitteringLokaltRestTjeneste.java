package no.nav.foreldrepenger.web.local.development;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.økonomistøtte.queue.consumer.ØkonomioppdragAsyncJmsConsumer;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.jpa.Transaction;

@Path("/okonomioppdrag")
@ApplicationScoped
@Transaction
public class TestØkonomiKvitteringLokaltRestTjeneste {
    private ØkonomioppdragAsyncJmsConsumer consumer;

    public TestØkonomiKvitteringLokaltRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public TestØkonomiKvitteringLokaltRestTjeneste(ØkonomioppdragAsyncJmsConsumer consumer) {
        this.consumer = consumer;
    }

    @POST
    @Timed
    @Path("/melding")
    @Produces(MediaType.APPLICATION_JSON)
    public Response mottaKvittering(@Context HttpServletRequest request) {
        try (Scanner scanner = new Scanner(request.getInputStream(), Charset.defaultCharset().name())) {
            scanner.useDelimiter("\\Z");
            final String kvittering = scanner.next();
            consumer.handle(kvittering);

            return Response.ok().build();
        } catch (IOException | VLException e) {
            return Response.serverError().entity(e).build();
        }
    }
}
