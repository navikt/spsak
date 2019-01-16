package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktKode;
import no.nav.foreldrepenger.behandling.aksjonspunkt.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.Redirect;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = { "aksjonspunkt" })
@Path("/behandling/aksjonspunkt")
@RequestScoped
@Transaction
@Produces(MediaType.APPLICATION_JSON)
public class AksjonspunktRestTjeneste {

    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;
    private BehandlingRepository behandlingRepository;
    private BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste;
    private TotrinnTjeneste totrinnTjeneste;

    public AksjonspunktRestTjeneste() {
        // Bare for RESTeasy
    }

    @Inject
    public AksjonspunktRestTjeneste(
        AksjonspunktApplikasjonTjeneste aksjonpunktApplikasjonTjeneste,
        BehandlingRepository behandlingRepository,
        BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste, TotrinnTjeneste totrinnTjeneste) {

        this.applikasjonstjeneste = aksjonpunktApplikasjonTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.behandlingutredningTjeneste = behandlingutredningTjeneste;
        this.totrinnTjeneste = totrinnTjeneste;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Hent aksjonspunter for en behandling", response = AksjonspunktDto.class, responseContainer = "Set")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public Response getAksjonspunkter(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto behandlingIdDto) throws URISyntaxException { // NOSONAR

        Long behandlingId = behandlingIdDto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Collection<Totrinnsvurdering> ttVurderinger = totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling);
        Set<AksjonspunktDto> dto = AksjonspunktDtoMapper.lagAksjonspunktDto(behandling, ttVurderinger);
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMaxAge(0);
        return Response.ok(dto).cacheControl(cc).build();
    }

    /**
     * Håndterer prosessering av aksjonspunkt og videre behandling.
     * <p>
     * MERK: Det skal ikke ligge spesifikke sjekker som avhenger av status på behanlding, steg eller knytning til
     * spesifikke aksjonspunkter idenne tjenesten.
     * @throws URISyntaxException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lagre endringer gitt av aksjonspunktene og rekjør behandling fra gjeldende steg")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response bekreft(@ApiParam("Liste over aksjonspunkt som skal bekreftes, inklusiv data som trengs for å løse de.") @Valid BekreftedeAksjonspunkterDto apDto) throws URISyntaxException { // NOSONAR

        Long behandlingId = apDto.getBehandlingId().getBehandlingId();
        Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer = apDto.getBekreftedeAksjonspunktDtoer();
        behandlingutredningTjeneste.kanEndreBehandling(behandlingId, apDto.getBehandlingVersjon());
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        validerBetingelserForAksjonspunkt(behandling, apDto.getBekreftedeAksjonspunktDtoer());

        applikasjonstjeneste.bekreftAksjonspunkter(bekreftedeAksjonspunktDtoer, behandlingId);

        return Redirect.tilBehandlingPollStatus(behandlingId);
    }

    /**
     * Oppretting og prosessering av aksjonspunkt som har type overstyringspunkt.
     * <p>
     * MERK: Det skal ikke ligge spesifikke sjekker som avhenger av status på behanlding, steg eller knytning til
     * spesifikke aksjonspunkter idenne tjenesten.
     */
    @POST
    @Path("/overstyr")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Overstyrer stegene")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response overstyr(@ApiParam("Liste over overstyring aksjonspunkter.") @Valid OverstyrteAksjonspunkterDto apDto) throws URISyntaxException { // NOSONAR

        Long behandlingId = apDto.getBehandlingId().getBehandlingId();

        behandlingutredningTjeneste.kanEndreBehandling(behandlingId, apDto.getBehandlingVersjon());

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        validerBetingelserForAksjonspunkt(behandling, apDto.getOverstyrteAksjonspunktDtoer());

        applikasjonstjeneste.overstyrAksjonspunkter(apDto.getOverstyrteAksjonspunktDtoer(), behandlingId);

        return Redirect.tilBehandlingPollStatus(behandlingId);
    }

    private void validerBetingelserForAksjonspunkt(Behandling behandling, Collection<? extends AksjonspunktKode> aksjonspunktDtoer) {
        // TODO (FC): skal ikke ha spesfikke pre-conditions inne i denne tjenesten (sjekk på status FATTER_VEDTAK). Se
        // om kan håndteres annerledes.
        if (behandling.getStatus().equals(BehandlingStatus.FATTER_VEDTAK) && !erFatteVedtakAkpt(aksjonspunktDtoer)) {
            throw AksjonspunktRestTjenesteFeil.FACTORY.totrinnsbehandlingErStartet(String.valueOf(behandling.getId())).toException();
        }
    }

    private boolean erFatteVedtakAkpt(Collection<? extends AksjonspunktKode> aksjonspunktDtoer) {
        return aksjonspunktDtoer.size() == 1 &&
            aksjonspunktDtoer.iterator().next().getKode().equals(AksjonspunktDefinisjon.FATTER_VEDTAK.getKode());
    }
}
