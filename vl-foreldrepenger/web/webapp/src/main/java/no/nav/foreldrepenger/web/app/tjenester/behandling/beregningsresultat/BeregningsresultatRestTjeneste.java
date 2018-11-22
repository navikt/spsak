package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.app.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatEngangsstønadDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto.BeregningsresultatMedUttaksplanDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "beregningsresultat" })
@Path("/behandling/beregningsresultat")
@RequestScoped
@Transaction
public class BeregningsresultatRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;

    public BeregningsresultatRestTjeneste() {
        // for resteasy
    }

    @Inject
    public BeregningsresultatRestTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                          BeregningsresultatTjeneste beregningsresultatMedUttaksplanTjeneste) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.beregningsresultatTjeneste = beregningsresultatMedUttaksplanTjeneste;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/engangsstonad")
    @ApiOperation(value = "Hent beregningsresultat med uttaksplan for engangsstønad behandling", notes = ("Returnerer beregningsresultat for engangsstønad behandling."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public BeregningsresultatEngangsstønadDto hentBeregningsresultatEngangsstønad(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingId) {
        return beregningsresultatTjeneste.lagBeregningsresultatEnkel(behandlingRepository.hentBehandling(behandlingId.getBehandlingId()))
            .orElse(null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/foreldrepenger")
    @ApiOperation(value = "Hent beregningsresultat med uttaksplan for foreldrepenger behandling", notes = ("Returnerer beregningsresultat med uttaksplan for behandling."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public BeregningsresultatMedUttaksplanDto hentBeregningsresultatForeldrepenger(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingId) {
        return beregningsresultatTjeneste.lagBeregningsresultatMedUttaksplan(behandlingRepository.hentBehandling(behandlingId.getBehandlingId()))
            .orElse(null);
    }
}
