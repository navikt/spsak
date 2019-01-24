package no.nav.foreldrepenger.fpmock2.server.rest;

import java.util.Base64;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;

@Api
@Path("/stsrest")
public class RestSTSService {

    private static final Logger LOG = LoggerFactory.getLogger(RestSTSService.class);

    @GET
    @Path("/rest/v1/sts/token")
    @Produces({ MediaType.APPLICATION_JSON })
    @SuppressWarnings("unused")
    public Response accessToken(
        @Context HttpServletRequest req,
        @FormParam("grant_type") String grantType,
        @FormParam("realm") String realm,
        @FormParam("code") String code,
        @FormParam("redirect_uri") String redirectUri) {
        // dummy sikkerhet, returnerer alltid en idToken/refresh_token
        String token = createIdToken(req);
        Oauth2AccessTokenResponse oauthResponse = new Oauth2AccessTokenResponse(token, UUID.randomUUID().toString(), "SlippMegInn");
        return Response.ok(oauthResponse).build();
    }

    private String createIdToken(HttpServletRequest req) {
        String issuer;
        String username = null;
        String[] authHeader = req.getHeader("Authorization").split(" ");
        if (authHeader.length == 2 && authHeader[0].equals("Basic")) {
            username = new String(Base64.getDecoder().decode(authHeader[1].trim().getBytes())).split(":")[0];
            LOG.info("setter brukernavn til {} utifra Authorization-Basic header", username);
        }
        if (null != System.getenv("AUTOTEST_OAUTH2_ISSUER_SCHEME")) {
            issuer = System.getenv("AUTOTEST_OAUTH2_ISSUER_SCHEME") + "://"
                + System.getenv("AUTOTEST_OAUTH2_ISSUER_URL") + ":"
                + System.getenv("AUTOTEST_OAUTH2_ISSUER_PORT")
                + System.getenv("AUTOTEST_OAUTH2_ISSUER_PATH");
            LOG.info("Setter issuer-url fra naisconfig: " + issuer);
        } else {
            issuer = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/isso/oauth2";
            LOG.info("Setter issuer-url fra implisit localhost: " + issuer);
        }
        String token = new OidcTokenGenerator(username).withIssuer(issuer).create();
        return token;
    }

}
