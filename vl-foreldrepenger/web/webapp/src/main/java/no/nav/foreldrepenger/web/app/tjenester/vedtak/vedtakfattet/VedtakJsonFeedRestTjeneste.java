package no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.jsonfeed.VedtakFattetTjeneste;
import no.nav.foreldrepenger.jsonfeed.dto.ForeldrepengerVedtakDto;
import no.nav.foreldrepenger.kontrakter.feed.felles.FeedDto;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.AktørParam;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.HendelseTypeParam;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.MaxAntallParam;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.SekvensIdParam;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "feed")
@Path(VedtakJsonFeedRestTjeneste.FEED_URI)
@ApplicationScoped
@Transaction
public class VedtakJsonFeedRestTjeneste {

    static final String FEED_URI = "/feed";
    private VedtakFattetTjeneste tjeneste;

    public VedtakJsonFeedRestTjeneste() {
    }

    @Inject
    public VedtakJsonFeedRestTjeneste(VedtakFattetTjeneste vedtakFattetTjeneste) {
        this.tjeneste = vedtakFattetTjeneste;
    }
    
    @GET
    @Path("/vedtak/foreldrepenger")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @ApiOperation(value = "Henter ut foreldrepengerhendelser om vedtak", response = FeedDto.class)
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public FeedDto vedtakHendelser(
            @QueryParam("sistLesteSekvensId") @ApiParam(value = "Siste sekvensId lest") @Valid @NotNull SekvensIdParam sistLesteSekvensIdParam,
            @DefaultValue("100") @QueryParam("maxAntall") @ApiParam(value = "max antall returnert", defaultValue="100") @Valid MaxAntallParam maxAntallParam,
            @DefaultValue("") @QueryParam("type") @ApiParam(value = "Filtrerer på type hendelse") @Valid HendelseTypeParam hendelseTypeParam,
            @DefaultValue("") @QueryParam("aktoerId") @ApiParam(value = "aktoerId") @Valid AktørParam aktørParam) {
        final ForeldrepengerVedtakDto dto = tjeneste.hentVedtak(sistLesteSekvensIdParam.get(), maxAntallParam.get(), hendelseTypeParam.get(), aktørParam.get());
        return new FeedDto.Builder().medTittel("ForeldrepengerVedtak_v1").medElementer(dto.getElementer()).medInneholderFlereElementer(dto.isHarFlereElementer()).build();
    }
}
