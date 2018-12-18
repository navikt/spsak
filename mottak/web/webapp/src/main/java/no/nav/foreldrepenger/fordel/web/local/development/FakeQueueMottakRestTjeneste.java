package no.nav.foreldrepenger.fordel.web.local.development;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import no.nav.foreldrepenger.mottak.queue.MeldingsFordeler;
import no.nav.foreldrepenger.mottak.queue.MottakAsyncJmsConsumer;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.Forsendelsesinformasjon;
import no.nav.vedtak.felles.jpa.Transaction;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

@Api(tags = {"mottak"}, description = "KUN FOR TEST")
@Path("/mottak")
@RequestScoped
@Transaction
public class FakeQueueMottakRestTjeneste {

    private MeldingsFordeler meldingsFordeler;

    public FakeQueueMottakRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public FakeQueueMottakRestTjeneste(MeldingsFordeler meldingsFordeler) {
        this.meldingsFordeler = meldingsFordeler;
    }

    /** 
     * @deprecated Kun for TEST!
     */
    @POST
    @Timed
    @Path("/melding")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    @Deprecated
    public Response mottaTynnmeldingFraTestHub(@Context HttpServletRequest request) {
        try (Scanner scanner = new Scanner(request.getInputStream(), Charset.defaultCharset().name())) {
            scanner.useDelimiter("\\Z");
            final String dokumentForsendelse = scanner.next();
            final Forsendelsesinformasjon forsendelsesinformasjon = MottakAsyncJmsConsumer.parseMessage(dokumentForsendelse);
            meldingsFordeler.execute(forsendelsesinformasjon);
        } catch (IOException e) {
            return Response.serverError().entity(e).build();
        }

        return Response.ok().build();
    }

}
