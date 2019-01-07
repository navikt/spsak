package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingAvsluttetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingOpprettetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.task.SakOgBehandlingTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * Observerer relevante status endringer på behandling og fagsak og varsler til Sakogbehandling (eksternt system).
 *
 */
@ApplicationScoped
public class OppdaterSakOgBehandlingEventObserver {

    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;

    static final String FORELDREPENGER_SAKSTEMA = "FOR";

    @Inject
    public OppdaterSakOgBehandlingEventObserver(GrunnlagRepositoryProvider repositoryProvider,
                                                ProsessTaskRepository prosessTaskRepository) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    public void observerBehandlingStatus(@Observes BehandlingAvsluttetEvent event) {
        oppdaterSakOgBehandlingVedBehandlingsstatusEndring(event);
    }

    public void observerBehandlingStatus(@Observes BehandlingOpprettetEvent event) {
        oppdaterSakOgBehandlingVedBehandlingsstatusEndring(event);
    }

    /**
     * Sender melding til køen som systemet "Sak og behandling" lytter på
     */
    private void oppdaterSakOgBehandlingVedBehandlingsstatusEndring(BehandlingStatusEvent event) {

        BehandlingStatus nyStatus = event.getNyStatus();

        BehandlingskontrollKontekst kontekst = event.getKontekst();
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        sendMeldingTilSakOgBehandling(behandling, nyStatus);
    }

    private void sendMeldingTilSakOgBehandling(Behandling behandling, BehandlingStatus nyStatus) {
        BehandlingTema behandlingTema = kodeverkRepository.finn(BehandlingTema.class, behandlingTemaFraBehandling(behandling));

        ProsessTaskData prosessTaskData = new ProsessTaskData(SakOgBehandlingTask.TASKNAME);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLINGS_TYPE_KODE_KEY, behandling.getType().getOffisiellKode());
        prosessTaskData.setProperty(SakOgBehandlingTask.SAKSTEMA_KEY, FORELDREPENGER_SAKSTEMA);
        prosessTaskData.setProperty(SakOgBehandlingTask.ANSVARLIG_ENHET_KEY, behandling.getBehandlendeEnhet());
        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLING_STATUS_KEY, nyStatus.getKode());
        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLING_OPPRETTET_TIDSPUNKT_KEY, behandling.getOpprettetTidspunkt().toLocalDate().toString());
        prosessTaskData.setProperty(SakOgBehandlingTask.BEHANDLINGSTEMAKODE, behandlingTema.getOffisiellKode());

        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }

    private BehandlingTema behandlingTemaFraBehandling(Behandling sisteBehandling) {
        return BehandlingTema.fraFagsak(sisteBehandling.getFagsak());
    }
}
