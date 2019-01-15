package no.nav.foreldrepenger.web.app.tjenester.konfig;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.APPLIKASJON;

import java.net.URI;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "konfig")
@Path("/konfig")
@RequestScoped
@Transaction
public class KonfigRestTjeneste {

    private URI rettskildeUrl;
    private URI systemrutineUrl;

    public KonfigRestTjeneste() {
        //NOSONAR
    }

    @Inject
    public KonfigRestTjeneste(@KonfigVerdi(value = "rettskilde.url") URI rettskildeUrl, @KonfigVerdi(value = "systemrutine.url") URI systemrutineUrl) {
        this.rettskildeUrl = rettskildeUrl;
        this.systemrutineUrl = systemrutineUrl;
    }

    @GET
    @Path("/rettskilde")
    @Produces("application/json")
    @ApiOperation(value = "Henter lenke til rettskilde.")
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Konfig hentRettskildeUrl() {
        return new Konfig(rettskildeUrl.toString());
    }

    @GET
    @Path("/systemrutine")
    @Produces("application/json")
    @ApiOperation(value = "Henter lenge til systemrutine")
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Konfig hentSystemrutine() {
        return new Konfig(systemrutineUrl.toString());
    }

    public static class Konfig {

        private String verdi;

        public Konfig(String verdi) {
            this.verdi = verdi;
        }

        public String getVerdi() {
            return verdi;
        }
    }
}
