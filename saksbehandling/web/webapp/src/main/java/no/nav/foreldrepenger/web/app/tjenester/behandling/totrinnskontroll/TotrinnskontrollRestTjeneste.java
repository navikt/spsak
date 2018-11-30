package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.app.TotrinnskontrollAksjonspunkterTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto.TotrinnskontrollSkjermlenkeContextDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "totrinnskontroll" })
@Path("/behandling/totrinnskontroll")
@RequestScoped
@Transaction
public class TotrinnskontrollRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste;

    public TotrinnskontrollRestTjeneste() {
        //
    }

    @Inject
    public TotrinnskontrollRestTjeneste(BehandlingRepository behandlingRepository, TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.totrinnskontrollTjeneste = totrinnskontrollTjeneste;
    }


    @POST
    @Path("/arsaker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Hent aksjonspunkter som skal til totrinnskontroll.", notes = ("Returner aksjonspunkter til totrinnskontroll for behandling."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnskontrollSkjermlenkeContext(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId.getBehandlingId());
        return totrinnskontrollTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
    }


    @POST
    @Path("/arsaker_read_only")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Hent totrinnsvurderinger for aksjonspunkter.", notes = ("Returner vurderinger for aksjonspunkter etter totrinnskontroll for behandling."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnskontrollvurderingSkjermlenkeContext(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId.getBehandlingId());
        return totrinnskontrollTjeneste.hentTotrinnsvurderingSkjermlenkeContext(behandling);
    }
}
