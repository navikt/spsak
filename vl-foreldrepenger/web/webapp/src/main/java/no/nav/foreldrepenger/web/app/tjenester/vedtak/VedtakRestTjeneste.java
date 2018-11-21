package no.nav.foreldrepenger.web.app.tjenester.vedtak;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.domene.vedtak.innsyn.VedtakInnsynTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "vedtak" })
@Path("/vedtak")
@RequestScoped
public class VedtakRestTjeneste {
    private VedtakInnsynTjeneste vedtakInnsynTjeneste;

    public VedtakRestTjeneste() {
        // for resteasy
    }

    @Inject
    public VedtakRestTjeneste(VedtakInnsynTjeneste vedtakTjeneste) {
        this.vedtakInnsynTjeneste = vedtakTjeneste;
    }

    @GET
    @Timed
    @Path("/hent-vedtaksdokument")
    @ApiOperation(value = "Hent vedtaksdokument gitt behandlingId",
        notes = ("Returnerer vedtaksdokument som er tilknyttet behadnlingId."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public String hentVedtaksdokument(@NotNull @QueryParam("behandlingId") @ApiParam("BehandlingId for vedtaksdokument") @Valid BehandlingIdDto behandlingId) {
        return vedtakInnsynTjeneste.hentVedtaksdokument(behandlingId.getBehandlingId());
    }
}
