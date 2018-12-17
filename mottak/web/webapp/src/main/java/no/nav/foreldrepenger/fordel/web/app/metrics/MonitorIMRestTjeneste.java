package no.nav.foreldrepenger.fordel.web.app.metrics;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.util.FPDateUtil;

@Api(tags = {"inntektsmelding-monitor"})
@Path("/im")
@Produces(APPLICATION_JSON)
@RequestScoped
public class MonitorIMRestTjeneste {

    private InntektsmeldingCache inntektsmeldingCache;

    public MonitorIMRestTjeneste() {
        // CDI
    }

    @Inject
    public MonitorIMRestTjeneste(InntektsmeldingCache inntektsmeldingCache) {
        this.inntektsmeldingCache = inntektsmeldingCache;
    }

    @GET
    @ApiOperation(value = "Henter antall inntektsmeldinger mottatt og hvor de er sendt", notes = ("Returnerer totalt antall inntektsmeldinger mottatt"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Antall inntektsmelding til journalføring", response = TelleIMDto.class)})
    @Path("/tell-im-total")
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = READ, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public TelleIMDto imTellAlleIM() {
        TelleIMDto alle = new TelleIMDto();
        alle.setGosys(inntektsmeldingCache.hentInntektsMeldingerSendtTilSystem(Fagsystem.GOSYS, null));
        alle.setFpsak(inntektsmeldingCache.hentInntektsMeldingerSendtTilSystem(Fagsystem.FPSAK, null));
        return alle;
    }

    @GET
    @ApiOperation(value = "Henter antall inntektsmeldinger mottatt og hvor de er sendt", notes = ("Returnerer totalt antall inntektsmeldinger mottatt"))
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Antall inntektsmelding til journalføring", response = TelleIMDto.class)})
    @Path("/tell-im-dagens")
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = READ, ressurs = DRIFT)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public TelleIMDto imTellDagensIM() {
        TelleIMDto alle = new TelleIMDto();
        alle.setGosys(inntektsmeldingCache.hentInntektsMeldingerSendtTilSystem(Fagsystem.GOSYS, FPDateUtil.iDag()));
        alle.setFpsak(inntektsmeldingCache.hentInntektsMeldingerSendtTilSystem(Fagsystem.FPSAK, FPDateUtil.iDag()));
        return alle;
    }

}
