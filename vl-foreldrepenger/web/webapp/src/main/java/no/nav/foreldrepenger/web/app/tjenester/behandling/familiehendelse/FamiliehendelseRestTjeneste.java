package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.søknad.SoknadDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "familiehendelse" })
@Path("/behandling/familiehendelse")
@RequestScoped
@Transaction
public class FamiliehendelseRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private FamiliehendelseDataDtoTjeneste dtoMapper;

    public FamiliehendelseRestTjeneste() {
        // for CDI proxy
    }

    @Inject
    public FamiliehendelseRestTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider, FamiliehendelseDataDtoTjeneste dtoMapper) {
        this.dtoMapper = dtoMapper;
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();

    }

    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returnerer info om familiehendelse, null hvis ikke eksisterer (GUI støtter ikke NOT_FOUND p.t.)", response = SoknadDto.class) })
    @ApiOperation(value = "Hent informasjon om familiehendelse til grunn for ytelse", notes = ("Returnerer info om familiehendelse til grunn for ytelse."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public FamiliehendelseDto getAvklartFamiliehendelseDto(@NotNull @ApiParam("BehandlingId for aktuell behandling") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<FamiliehendelseDto> dtoOpt = dtoMapper.mapFra(behandling);
        return dtoOpt.orElse(null);
    }
}
