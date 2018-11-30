package no.nav.foreldrepenger.web.app.oppgave;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SaksnummerDto;
import no.nav.vedtak.filter.DoNotCache;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

@Path("")
@DoNotCache
@ApplicationScoped
@Api(tags = {"redirect"}, description = "Oppgave redirect")
public class OppgaveRedirectTjeneste {

    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private FagsakRepository fagsakRepository;
    private RedirectFactory redirectFactory; //For å kunne endre til alternativ implementasjon på Jetty

    public OppgaveRedirectTjeneste() {
    }

    @Inject
    public OppgaveRedirectTjeneste(OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository, FagsakRepository fagsakRepository, RedirectFactory redirectFactory) {
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.fagsakRepository = fagsakRepository;
        this.redirectFactory = redirectFactory;
    }

    @GET
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
    public Response doRedirect(@QueryParam("oppgaveId") @Valid OppgaveIdDto oppgaveId,
                               @QueryParam("sakId") @Valid SaksnummerDto saksnummerDto) {
        OppgaveRedirectData data = OppgaveRedirectData.hent(oppgaveBehandlingKoblingRepository, fagsakRepository, oppgaveId, saksnummerDto);
        String url = redirectFactory.lagRedirect(data);
        Response.ResponseBuilder responser = Response.temporaryRedirect(URI.create(url));
        responser.encoding("UTF-8");
        return responser.build();
    }

}
