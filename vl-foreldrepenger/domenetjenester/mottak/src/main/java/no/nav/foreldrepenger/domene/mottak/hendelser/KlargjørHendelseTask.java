package no.nav.foreldrepenger.domene.mottak.hendelser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.ForretningshendelseDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(KlargjørHendelseTask.TASKNAME)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class KlargjørHendelseTask implements ProsessTaskHandler {

    public static final String TASKNAME = "hendelser.klargjoering";
    public static final String PROPERTY_HENDELSE_TYPE = "hendelseType";
    public static final String PROPERTY_UID = "hendelseUid";

    private ForretningshendelseMottak forretningshendelseMottak;

    KlargjørHendelseTask() {
        // for CDI proxy
    }
    
    @Inject
    public KlargjørHendelseTask(ForretningshendelseMottak forretningshendelseMottak) {
        this.forretningshendelseMottak = forretningshendelseMottak;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        ForretningshendelseDto dto = new ForretningshendelseDto(prosessTaskData.getPropertyValue(PROPERTY_HENDELSE_TYPE), prosessTaskData.getPayloadAsString());
        forretningshendelseMottak.mottaForretningshendelse(dto);
    }
}
