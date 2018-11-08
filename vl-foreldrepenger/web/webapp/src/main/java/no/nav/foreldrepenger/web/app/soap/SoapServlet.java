package no.nav.foreldrepenger.web.app.soap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;

import no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.BehandleForeldrepengesakV1;
import no.nav.tjeneste.virksomhet.foreldrepengesak.v1.binding.ForeldrepengesakV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.AbstractSoapServlet;

@ApplicationScoped
@WebServlet(urlPatterns = {"/tjenester", "/tjenester/", "/tjenester/*"}, loadOnStartup = 1)
public class SoapServlet extends AbstractSoapServlet {
    @Inject
    public void publishForeldrepengerSakV1(ForeldrepengesakV1 foreldrepengesakService) {
        publish(foreldrepengesakService);
    }

    @Inject
    public void publishBehandleForeldrepengesakService(BehandleForeldrepengesakV1 behandleForeldrepengesakService) {
        publish(behandleForeldrepengesakService);
    }
}
