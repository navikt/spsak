package no.nav.foreldrepenger.behandlingskontroll.task;

import static no.nav.foreldrepenger.behandlingskontroll.task.StartBehandlingTask.TASKTYPE;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Kjører behandlingskontroll automatisk fra start.
 */
@ApplicationScoped
@ProsessTask(TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class StartBehandlingTask implements ProsessTaskHandler {
    public static final String TASKTYPE = "behandlingskontroll.startBehandling";

    public StartBehandlingTask() {
    }

    @Override
    public void doTask(ProsessTaskData data) {

        // dynamisk lookup siden finnes ikke nødvendigvis (spesielt når vi kompilerer)
        CDI<Object> cdi = CDI.current();
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = cdi.select(BehandlingskontrollTjeneste.class).get();

        try {
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(data.getBehandlingId());
            // TODO (FC): assert at behandlingen starter fra første steg?
            behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        } finally {
            // ikke nødvendig siden vi kjører request scoped, men tar en tidlig destroy
            cdi.destroy(behandlingskontrollTjeneste);
        }
    }
}