package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.kontrakter.abonnent.HendelseWrapperDto;
import no.nav.foreldrepenger.web.app.tjenester.hendelser.HendelserRestTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * Wrapper rundt SUT {@link HendelserRestTjeneste} for testformål.
 * Kaller tjenesten som utenfra, i tillegg fasiliteres prosesskjøring og testbuildere for DTO-er
 */
@ApplicationScoped
public class HendelserRestTjenesteTestAPI {

    private HendelserRestTjeneste hendelserRestTjeneste;
    private ProsessTaskRepository prosessTaskRepository;

    private HendelserRestTjenesteTestAPI() {
        // For CDI
    }

    @Inject
    public HendelserRestTjenesteTestAPI(HendelserRestTjeneste hendelserRestTjeneste, ProsessTaskRepository prosessTaskRepository) {
        this.hendelserRestTjeneste = hendelserRestTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public void mottaHendelse(HendelseWrapperDto dto) {
        // SUT
        hendelserRestTjeneste.mottaHendelse(dto);
        new KjørProsessTasks(prosessTaskRepository).utførAlleTasks();
    }
}
