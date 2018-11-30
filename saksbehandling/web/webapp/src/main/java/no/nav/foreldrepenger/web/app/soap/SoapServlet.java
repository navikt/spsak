package no.nav.foreldrepenger.web.app.soap;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.annotation.WebServlet;

import no.nav.vedtak.felles.integrasjon.felles.ws.AbstractSoapServlet;

@ApplicationScoped
@WebServlet(urlPatterns = {"/tjenester", "/tjenester/", "/tjenester/*"}, loadOnStartup = 1)
public class SoapServlet extends AbstractSoapServlet {

//    @Inject
//    public void publishBehandleForeldrepengesakService(BehandleForeldrepengesakV1 behandleForeldrepengesakService) {
//        publish(behandleForeldrepengesakService);
//    }
}
