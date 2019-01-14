package no.nav.foreldrepenger.web.app.tjenester.historikk;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandling.historikk.HistorikkInnslagDokumentLinkDto;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandling.historikk.HistorikkinnslagDto;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SaksnummerDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "historikk")
@Path("/historikk")
@ApplicationScoped
@Transaction
public class HistorikkRestTjeneste {
    private HistorikkTjenesteAdapter historikkTjeneste;

    public HistorikkRestTjeneste() {
        // Rest CDI
    }

    @Inject
    public HistorikkRestTjeneste(HistorikkTjenesteAdapter historikkTjeneste) {
        this.historikkTjeneste = historikkTjeneste;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Henter alle historikkinnslag for gitt behandling.")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentAlleInnslag(@Context HttpServletRequest request,
                                    @NotNull @QueryParam("saksnummer") @ApiParam("Saksnummer må være et eksisterende saksnummer") @Valid SaksnummerDto saksnummerDto) {
        Response.ResponseBuilder responseBuilder = Response.ok();
        // FIXME XSS valider requestURL eller bruk relativ URL
        String requestURL = getRequestPath(request);
        String url = requestURL + "/dokument/hent-dokument";

        List<HistorikkinnslagDto> historikkInnslagDtoList = historikkTjeneste.hentAlleHistorikkInnslagForSak(new Saksnummer(saksnummerDto.getVerdi()));
        if (historikkInnslagDtoList != null && historikkInnslagDtoList.size() > 0) {
            responseBuilder.entity(historikkInnslagDtoList);
            for (HistorikkinnslagDto dto : historikkInnslagDtoList) {
                for (HistorikkInnslagDokumentLinkDto linkDto : dto.getDokumentLinks()) {
                    String journalpostId = linkDto.getJournalpostId();
                    String dokumentId = linkDto.getDokumentId();
                    // FIXME XSS escape URL-parametre
                    linkDto.setUrl(url + "?journalpostId=" + journalpostId + "&dokumentId=" + dokumentId);
                }
            }
        } else {
            responseBuilder.entity(Collections.emptyList());
        }
        return responseBuilder.build();
    }

    String getRequestPath(HttpServletRequest request) {
        if(request==null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(request.getScheme())
            .append("://")
            .append(request.getLocalName())
            .append(":") // NOSONAR
            .append(request.getLocalPort());

        stringBuilder.append(request.getContextPath())
            .append(request.getServletPath());
        return stringBuilder.toString();
    }
}
