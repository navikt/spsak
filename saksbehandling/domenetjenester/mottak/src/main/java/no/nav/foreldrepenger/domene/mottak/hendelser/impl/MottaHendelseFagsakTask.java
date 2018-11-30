package no.nav.foreldrepenger.domene.mottak.hendelser.impl;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.task.FagsakProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.mottak.hendelser.ForretningshendelseMottak;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(MottaHendelseFagsakTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class MottaHendelseFagsakTask extends FagsakProsessTask {

    public static final String TASKTYPE = "hendelser.håndterHendelsePåFagsak";
    static final String PROPERTY_HENDELSE_TYPE = "hendelseType";

    private ForretningshendelseMottak forretningshendelseMottak;

    MottaHendelseFagsakTask() {
        // for CDI proxy
    }

    @Inject
    public MottaHendelseFagsakTask(ForretningshendelseMottak forretningshendelseMottak, BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.forretningshendelseMottak = forretningshendelseMottak;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long fagsakId = prosessTaskData.getFagsakId();
        String hendelseTypeKode = prosessTaskData.getPropertyValue(PROPERTY_HENDELSE_TYPE);
        Objects.requireNonNull(hendelseTypeKode);
        Objects.requireNonNull(fagsakId);

        forretningshendelseMottak.håndterHendelsePåFagsak(fagsakId, hendelseTypeKode);
    }
}
