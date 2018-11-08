package no.nav.foreldrepenger.web.app.tjenester.behandling.klage;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "klage" })
@Path("/behandling/klage")
@RequestScoped
@Transaction
public class KlageRestTjeneste {

    private BehandlingRepository behandlingRepository;

    public KlageRestTjeneste() {
        // for CDI proxy
    }

    @Inject
    public KlageRestTjeneste(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Returnerer vurdering av en klage fra ulike instanser") })
    @ApiOperation(value = "Hent informasjon om klagevurdering for en klagebehandling", notes = ("Returnerer info om vurdering av klage"), response=KlagebehandlingDto.class)
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response getKlageVurdering(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto behandlingIdDto) {
        Long behandlingId = behandlingIdDto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        KlagebehandlingDto dto = mapFra(behandling);

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMaxAge(0);
        return Response.ok(dto).cacheControl(cc).build();
    }

    private KlagebehandlingDto mapFra(Behandling behandling) {
        KlagebehandlingDto dto = new KlagebehandlingDto();
        Optional<KlageVurderingResultatDto> nfpVurdering = KlageVurderingResultatDtoMapper.mapNFPKlageVurderingResultatDto(behandling);
        Optional<KlageVurderingResultatDto> nkVurdering = KlageVurderingResultatDtoMapper.mapNKKlageVurderingResultatDto(behandling);

        if (nfpVurdering.isPresent() || nkVurdering.isPresent()) {
            nfpVurdering.ifPresent(dto::setKlageVurderingResultatNFP);
            nkVurdering.ifPresent(dto::setKlageVurderingResultatNK);
            return dto;
        } else {
            return null;
        }
    }

}
