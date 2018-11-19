package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.VurderOgSendØkonomiOppdrag;
import no.nav.foreldrepenger.behandlingskontroll.task.BehandlingProsessTask;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHendelse;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ProsessTask(VurderOgSendØkonomiOppdragTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class VurderOgSendØkonomiOppdragTask extends BehandlingProsessTask {

    private static final Logger log = LoggerFactory.getLogger(VurderOgSendØkonomiOppdragTask.class);

    public static final String TASKTYPE = "iverksetteVedtak.oppdragTilØkonomi";

    public static final String SEND_OPPDRAG = "sendOppdrag";

    private VurderOgSendØkonomiOppdrag tjeneste;
    private ProsessTaskRepository prosessTaskRepository;

    VurderOgSendØkonomiOppdragTask() {
        // for CDI proxy
    }

    @Inject
    public VurderOgSendØkonomiOppdragTask(VurderOgSendØkonomiOppdrag tjeneste,
                                          ProsessTaskRepository prosessTaskRepository,
                                          BehandlingRepositoryProvider repositoryProvider) {
        super(repositoryProvider);
        this.tjeneste = tjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Timed
    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        // Har vi mottatt kvittering?
        Optional<ProsessTaskHendelse> hendelse = prosessTaskData.getHendelse();
        Long behandlingId = prosessTaskData.getBehandlingId();
        if (hendelse.isPresent()) {
            behandleHendelse(hendelse.get(), behandlingId);
            return;
        }
        vurderSendingAvOppdrag(prosessTaskData, behandlingId);
    }

    private void vurderSendingAvOppdrag(ProsessTaskData prosessTaskData, Long behandlingId) {
        if (erSendOppdragPropertySatt(prosessTaskData)) {
            vurderSendingAvOppdrag(prosessTaskData, behandlingId, false);
        } else {
            vurderSendingAvOppdrag(prosessTaskData, behandlingId, true);
        }
    }

    private void vurderSendingAvOppdrag(ProsessTaskData prosessTaskData, Long behandlingId, boolean skalOppdragSendesTilØkonomi) {
        if (tjeneste.skalSendeOppdrag(behandlingId)) {
            log.info("Klargjør økonomioppdrag for behandling: {}", behandlingId); //$NON-NLS-1$
            tjeneste.sendOppdrag(behandlingId, prosessTaskData.getId(), skalOppdragSendesTilØkonomi);
            prosessTaskData.venterPåHendelse(ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING);
            prosessTaskData.setCallIdFraEksisterende();
            prosessTaskRepository.lagre(prosessTaskData);
            log.info("Økonomioppdrag er klargjort for behandling: {}", behandlingId); //$NON-NLS-1$

        } else {
            log.info("Ikke aktuelt for behandling: {}", behandlingId); //$NON-NLS-1$
        }
    }

    private void behandleHendelse(ProsessTaskHendelse prosessTaskHendelse, Long behandlingId) {
        if (prosessTaskHendelse == ProsessTaskHendelse.ØKONOMI_OPPDRAG_KVITTERING) {
            log.info("Økonomioppdrag-kvittering mottatt for behandling: {}", behandlingId); //$NON-NLS-1$
        } else {
            throw new IllegalStateException("Uventet hendelse " + prosessTaskHendelse);
        }
    }

    private boolean erSendOppdragPropertySatt(ProsessTaskData prosessTaskData) {
        return prosessTaskData.getPropertyValue(SEND_OPPDRAG) != null && "false".equals(prosessTaskData.getPropertyValue(SEND_OPPDRAG));
    }
}
