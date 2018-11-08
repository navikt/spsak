package no.nav.foreldrepenger.web.app.tjenester.integrasjonstatus;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.BooleanUtils;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;

@Api(tags = { "integrasjon" })
@Path("/integrasjon")
@RequestScoped
@Transaction
public class IntegrasjonstatusRestTjeneste {

    private IntegrasjonstatusTjeneste integrasjonstatusTjeneste;
    private boolean skalViseDetaljerteFeilmeldinger;

    public IntegrasjonstatusRestTjeneste() {
        // CDI
    }

    @Inject
    public IntegrasjonstatusRestTjeneste(IntegrasjonstatusTjeneste integrasjonstatusTjeneste,
                                         @KonfigVerdi(value = "vise.detaljerte.feilmeldinger") Boolean viseDetaljerteFeilmeldinger) {
        this.integrasjonstatusTjeneste = integrasjonstatusTjeneste;
        this.skalViseDetaljerteFeilmeldinger = BooleanUtils.toBoolean(viseDetaljerteFeilmeldinger);
    }

    @GET
    @Timed
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gir en oversikt over systemer som er nede",
        notes = ("Inneholder også detaljer og evt kjent tidspunkt for når systemet er oppe igjen."))
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.APPLIKASJON)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<SystemNedeDto> finnSystemerSomErNede() {
        return integrasjonstatusTjeneste.finnSystemerSomErNede();
    }

    @GET
    @Timed
    @Path("/status/vises")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Returnerer en boolean som angir om detaljerte feilmeldinger skal vises av GUI")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, ressurs = BeskyttetRessursResourceAttributt.APPLIKASJON)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public boolean skalViseDetaljerteFeilmeldinger() {
        return skalViseDetaljerteFeilmeldinger;
    }
}
