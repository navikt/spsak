package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper;

import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.NyBehandlingDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * Wrapper rundt SUT {@link BehandlingRestTjeneste} for testformål.
 * Kaller tjenesten som utenfra, i tillegg fasiliteres prosesskjøring og testbuildere for DTO-er
 */
@ApplicationScoped
public class BehandlingRestTjenesteTestAPI {

    private BehandlingRestTjeneste behandlingRestTjeneste;
    private ProsessTaskRepository prosessTaskRepository;

    BehandlingRestTjenesteTestAPI() {
        //for cdi proxy
    }

    @Inject
    public BehandlingRestTjenesteTestAPI(BehandlingRestTjeneste behandlingRestTjeneste, ProsessTaskRepository prosessTaskRepository) {
        this.behandlingRestTjeneste = behandlingRestTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
    }


    public void gjenopptaBehandling(GjenopptaBehandlingDto gjenopptaBehandlingDto) throws URISyntaxException {
        behandlingRestTjeneste.gjenopptaBehandling(gjenopptaBehandlingDto);
        new KjørProsessTasks(prosessTaskRepository).utførAlleTasks();
    }

    public void opprettNyBehandling(NyBehandlingDto nyBehandlingDto) throws URISyntaxException {
        behandlingRestTjeneste.opprettNyBehandling(nyBehandlingDto);
        new KjørProsessTasks(prosessTaskRepository).utførAlleTasks();
    }

}
