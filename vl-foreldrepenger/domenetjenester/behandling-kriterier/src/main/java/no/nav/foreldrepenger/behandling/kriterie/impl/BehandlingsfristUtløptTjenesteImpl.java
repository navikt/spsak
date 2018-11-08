package no.nav.foreldrepenger.behandling.kriterie.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.kriterie.BehandlingsfristUtløptTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.dokumentbestiller.api.SendForlengelsesbrevTaskProperties;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class BehandlingsfristUtløptTjenesteImpl implements BehandlingsfristUtløptTjeneste {

    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    public BehandlingsfristUtløptTjenesteImpl(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public void behandlingsfristUtløpt(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(SendForlengelsesbrevTaskProperties.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }
}
