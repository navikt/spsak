package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.observer;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.codahale.metrics.MetricRegistry;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingAvsluttetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStatusEvent.BehandlingOpprettetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
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

    private FamilieHendelseRepository familieGrunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;
    private MetricRegistry metricRegistry;

    static final String FORELDREPENGER_SAKSTEMA = "FOR";

    @Inject
    public OppdaterSakOgBehandlingEventObserver(BehandlingRepositoryProvider repositoryProvider,
                                                ProsessTaskRepository prosessTaskRepository,
                                                MetricRegistry metricRegistry) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.metricRegistry = metricRegistry;
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
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
        Fagsak fagsak = behandling.getFagsak();

        sendMeldingTilSakOgBehandling(behandling, nyStatus);

        if (BehandlingStatus.OPPRETTET.equals(nyStatus)) {
            BehandlingTema behandlingTema = kodeverkRepository.finn(BehandlingTema.class, getBehandlingsTemaForFagsak(fagsak));
            String key = "fpsak." + (behandlingTema.getOffisiellKode() != null ? behandlingTema.getOffisiellKode() : "udefinert" ) + ".ny.behandling";
            metricRegistry.meter(key).mark();
        }
    }

    private void sendMeldingTilSakOgBehandling(Behandling behandling, BehandlingStatus nyStatus) {
        BehandlingTema behandlingTema = kodeverkRepository.finn(BehandlingTema.class, behandlingTemaFraBehandling(behandling));
        if (behandlingTema.equals(BehandlingTema.UDEFINERT)) {
            throw new IllegalStateException("Utviklerfeil: Finner ikke behandlingstema for fagsak");
        }

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

    private BehandlingTema getBehandlingsTemaForFagsak(Fagsak s) {
        Optional<Behandling> behandling = behandlingRepository.hentSisteBehandlingForFagsakId(s.getId());
        if (!behandling.isPresent()) {
            return BehandlingTema.fraFagsak(s, null);
        }

        Behandling sisteBehandling = behandling.get();
        return behandlingTemaFraBehandling(sisteBehandling);
    }

    private BehandlingTema behandlingTemaFraBehandling(Behandling sisteBehandling) {
        final Optional<FamilieHendelseGrunnlag> grunnlag = familieGrunnlagRepository.hentAggregatHvisEksisterer(sisteBehandling);
        return BehandlingTema.fraFagsak(sisteBehandling.getFagsak(), grunnlag.map(FamilieHendelseGrunnlag::getSøknadVersjon).orElse(null));
    }
}
