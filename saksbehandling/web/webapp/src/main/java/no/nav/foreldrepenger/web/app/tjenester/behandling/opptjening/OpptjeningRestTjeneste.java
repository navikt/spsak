package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;

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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "opptjening" })
@Path("/behandling/opptjening")
@RequestScoped
@Transaction
public class OpptjeningRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private OpptjeningDtoTjeneste dtoMapper;

    public OpptjeningRestTjeneste() {
        // for CDI proxy
    }

    @Inject
    public OpptjeningRestTjeneste(GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                                  OpptjeningDtoTjeneste dtoMapper) {
        this.behandlingRepository = grunnlagRepositoryProvider.getBehandlingRepository();
        this.dtoMapper = dtoMapper;

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returnerer Opptjening, null hvis ikke eksisterer (GUI støtter ikke NOT_FOUND p.t.)", response = OpptjeningDto.class) })
    @ApiOperation(value = "Hent informasjon om opptjening", notes = ("Returnerer info om opptjening, inkludert hva som er avklart."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public OpptjeningDto getOpptjening(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return dtoMapper.mapFra(behandling).orElse(null);
    }
}
