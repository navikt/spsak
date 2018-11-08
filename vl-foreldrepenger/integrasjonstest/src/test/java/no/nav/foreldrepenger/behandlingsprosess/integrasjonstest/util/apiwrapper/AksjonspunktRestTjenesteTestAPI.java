package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper;

import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.OverstyrteAksjonspunkterDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * Wrapper rundt SUT {@link AksjonspunktRestTjeneste} for testformål.
 * Kaller tjenesten som utenfra, i tillegg fasiliteres prosesskjøring og testbuildere for DTO-er
 */
@ApplicationScoped
public class AksjonspunktRestTjenesteTestAPI {

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;
    private ProsessTaskRepository prosessTaskRepository;

    private AksjonspunktRestTjenesteTestAPI() {
        // For CDI
    }

    @Inject
    public AksjonspunktRestTjenesteTestAPI(AksjonspunktRestTjeneste aksjonspunktRestTjeneste, ProsessTaskRepository prosessTaskRepository) {
        this.aksjonspunktRestTjeneste = aksjonspunktRestTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
    }


    public void bekreft(BekreftedeAksjonspunkterDto bekreftedeAksjonspunkterDto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(bekreftedeAksjonspunkterDto);
        new KjørProsessTasks(prosessTaskRepository).utførAlleTasks();
    }

    public void overstyr(OverstyrteAksjonspunkterDto bekreftedeAksjonspunkterDto) throws URISyntaxException {
        aksjonspunktRestTjeneste.overstyr(bekreftedeAksjonspunkterDto);
        new KjørProsessTasks(prosessTaskRepository).utførAlleTasks();
    }

}
