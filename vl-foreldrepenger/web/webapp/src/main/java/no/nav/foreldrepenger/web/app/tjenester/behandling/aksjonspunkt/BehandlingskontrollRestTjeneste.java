package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

/**
 * Tester Behandlingskontroll synkront.
 */
@Api(tags = {"behandlingskontroll"})
@Path("/behandling/behandlingskontroll")
@RequestScoped
@Transaction
public class BehandlingskontrollRestTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private BehandlingRepository behandlingRepository;

    public BehandlingskontrollRestTjeneste() {
        // For Rest-CDI
    }

    @Inject
    public BehandlingskontrollRestTjeneste(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                           BehandlingRepository behandlingRepository) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.behandlingRepository = behandlingRepository;
    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "KUN FOR TEST!!!: Kjører behandlingskontroll på en behandling.", notes = ("Kjører behandlingskontroll fra gjeldende steg frem til så langt behandlingen lar seg kjøre automatisk. /n" +
        "Først og fremst for synkron/automatisering av behandlingsprosessen."), tags = {"Behandlingskontroll"})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.UPDATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public BehandlingskontrollDto kjørBehandling(
        @NotNull @QueryParam("behandlingId") @ApiParam("BehandlingId må referere en allerede opprettet behandling") @Valid
                BehandlingIdDto behandlingIdDto) {

        Behandling behandling = behandlingRepository.hentBehandling(behandlingIdDto.getBehandlingId());
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingIdDto.getBehandlingId());
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        return new BehandlingskontrollDto(behandling.getStatus(), behandling.getAktivtBehandlingSteg(), behandling.getAksjonspunkter());
    }
}
