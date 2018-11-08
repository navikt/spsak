package no.nav.foreldrepenger.web.app.tjenester.behandling.dto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.domene.typer.Saksnummer;

public final class Redirect {
    private static final Logger log = LoggerFactory.getLogger(Redirect.class);

    private Redirect() {
        // no ctor
    }

    public static Response tilBehandlingPollStatus(Long behandlingId, Optional<String> gruppeOpt) throws URISyntaxException {
        URI uri = new URI("/behandlinger/status?behandlingId=" + behandlingId + (gruppeOpt.isPresent() ? "&gruppe=" + gruppeOpt.get() : ""));
        return Response.accepted().location(honorXForwardedProto(uri)).build();
    }

    public static Response tilBehandlingPollStatus(Long behandlingId) throws URISyntaxException {
        return tilBehandlingPollStatus(behandlingId, Optional.empty());
    }

    public static Response tilBehandlingEllerPollStatus(Long behandlingId, AsyncPollingStatus status) throws URISyntaxException {
        String resultatUri = "/behandlinger/?behandlingId=" + behandlingId;
        return buildResponse(status, resultatUri);
    }

    public static Response tilFagsakPollStatus(Saksnummer saksnummer, Optional<String> gruppeOpt) throws URISyntaxException {
        URI uri = new URI("/fagsak/status?saksnummer=" + saksnummer.getVerdi() + (gruppeOpt.isPresent() ? "&gruppe=" + gruppeOpt.get() : ""));
        return Response.accepted().location(honorXForwardedProto(uri)).build();
    }

    public static Response tilFagsakEllerPollStatus(Saksnummer saksnummer, AsyncPollingStatus status) throws URISyntaxException {
        String resultatUri = "/fagsak/?saksnummer=" + saksnummer.getVerdi();
        return buildResponse(status, resultatUri);
    }

    private static Response buildResponse(AsyncPollingStatus status, String resultatUri) throws URISyntaxException {
        URI uri = honorXForwardedProto(new URI(resultatUri));
        if (status != null) {
            // sett alltid resultat-location i tilfelle timeout på klient
            status.setLocation(uri);
            return Response.status(status.getStatus().getHttpStatus()).entity(status).build();
        } else {
            return Response.seeOther(uri).build();
        }
    }

    /**
     * @see org.jboss.resteasy.specimpl.ResponseBuilderImpl#location(URI)
     * @see URI#create(String)
     */
    private static URI honorXForwardedProto(URI location) throws URISyntaxException {
        URI newLocation = null;
        if (relativLocationAndRequestAvailable(location)) {
            HttpRequest httpRequest = ResteasyProviderFactory.getContextData(HttpRequest.class);
            String xForwardedProto = getXForwardedProtoHeader(httpRequest);

            if (mismatchedScheme(xForwardedProto, httpRequest)) {
                String path = location.toString();
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                URI baseUri = httpRequest.getUri().getBaseUri();
                try {
                    URI rewritten = new URI(xForwardedProto, baseUri.getSchemeSpecificPart(), baseUri.getFragment())
                        .resolve(path);
                    log.debug("Rewrote URI from '{}' to '{}'", location, rewritten);
                    newLocation = rewritten;
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        }
        return newLocation != null ? newLocation : leggTilBaseUri(location);
    }

    private static boolean relativLocationAndRequestAvailable(URI location) {
        return location != null &&
            !location.isAbsolute() &&
            ResteasyProviderFactory.getContextData(HttpRequest.class) != null;
    }

    /**
     * @return http, https or null
     */
    private static String getXForwardedProtoHeader(HttpRequest httpRequest) {
        String xForwardedProto = httpRequest.getHttpHeaders().getHeaderString("X-Forwarded-Proto");
        if (xForwardedProto != null &&
            (xForwardedProto.equalsIgnoreCase("https") ||
                xForwardedProto.equalsIgnoreCase("http"))) {
            return xForwardedProto;
        }
        return null;
    }

    private static boolean mismatchedScheme(String xForwardedProto, HttpRequest httpRequest) {
        return xForwardedProto != null &&
            !xForwardedProto.equalsIgnoreCase(httpRequest.getUri().getBaseUri().getScheme());
    }

    @SuppressWarnings("resource")
    private static URI leggTilBaseUri(URI resultatUri) throws URISyntaxException {
        // tvinger resultatUri til å være en absolutt URI (passer med Location Header og Location felt når kommer i payload)
        Response response = Response.noContent().location(resultatUri).build();
        return response.getLocation();
    }
}
