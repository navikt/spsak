package no.nav.foreldrepenger.web.local.development;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;


/**
 * Innlogging ved kjøring lokalt.
 * <p>
 * Se utviklerhåndbok for hvordan dette fungerer.
 */
@Path("/login")
@RequestScoped
public class JettyLoginResource {

    @GET
    @Path("")
    public Response login() {
        //  når vi har kommet hit, er brukeren innlogget og har fått ID-token. Kan da gjøre redirect til hovedsiden for VL
        return Response.temporaryRedirect(URI.create("http://localhost:9000/")).build();
    }
}
