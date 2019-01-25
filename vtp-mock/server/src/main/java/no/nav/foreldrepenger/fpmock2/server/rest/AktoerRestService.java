package no.nav.foreldrepenger.fpmock2.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import no.nav.foreldrepenger.fpmock2.testmodell.repo.impl.BasisdataProviderFileImpl;
import no.nav.foreldrepenger.fpmock2.testmodell.repo.impl.DelegatingTestscenarioRepository;
import no.nav.foreldrepenger.fpmock2.testmodell.repo.impl.TestscenarioRepositoryImpl;

@Api
@Path("/aktoerrest")
public class AktoerRestService {

    @GET
    @Path("/api/v1/identer")
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressWarnings("unused")
    public Response identer(
        @Context HttpServletRequest req,
        @QueryParam("gjeldende") String gjeldendeSomTrueFalse,
        @HeaderParam("Nav-Personidenter") String ident
    ) throws IOException {


        DelegatingTestscenarioRepository testScenarioRepository = new DelegatingTestscenarioRepository(TestscenarioRepositoryImpl.getInstance(BasisdataProviderFileImpl.getInstance()));
        String brukerFnr = testScenarioRepository.getPersonIndeks().finnByAktørIdent(ident).getIdent();

        var hovedResp = new HashMap<String,Object>();
        var identInfo = new HashMap<String, Object>();
        var identListe = new ArrayList<Object>();

        var aktorId = new HashMap<String,String>();
        aktorId.put("ident", ident);
        aktorId.put("identgruppe", IdentType.AktoerId.name());

        var fnr = new HashMap<String,String>();
        fnr.put("ident", brukerFnr); //new FiktiveFnr().tilfeldigFnr());
        fnr.put("identgruppe", IdentType.NorskIdent.name());

        identListe.add(aktorId);
        identListe.add(fnr);

        identInfo.put("identer", identListe);
        hovedResp.put(ident, identInfo);

        return Response.ok(hovedResp).build();
    }

    private static enum IdentType {
        AktoerId, NorskIdent
    }
}
