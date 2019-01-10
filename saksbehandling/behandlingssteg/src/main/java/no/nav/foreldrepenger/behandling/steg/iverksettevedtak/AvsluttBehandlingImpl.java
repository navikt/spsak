package no.nav.foreldrepenger.behandling.steg.iverksettevedtak;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryFeil;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class AvsluttBehandlingImpl implements AvsluttBehandling {

    private static final Logger log = LoggerFactory.getLogger(AvsluttBehandlingImpl.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private FagsakRepository fagsakRepository;
    private BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private ProsessTaskRepository prosessTaskRepository;

    public AvsluttBehandlingImpl() {
        // CDI
    }

    @Inject
    public AvsluttBehandlingImpl(GrunnlagRepositoryProvider repositoryProvider,
                                 ResultatRepositoryProvider resultatRepositoryProvider,
                                 BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                 BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer, ProsessTaskRepository prosessTaskRepository) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.behandlingVedtakRepository = resultatRepositoryProvider.getVedtakRepository();
        this.behandlingVedtakEventPubliserer = behandlingVedtakEventPubliserer;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public void avsluttBehandling(Long behandlingId) {
        log.info("Avslutter behandling: {}", behandlingId); //$NON-NLS-1$
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandlingId);

        BehandlingVedtak vedtak = behandlingVedtakRepository.hentVedtakFor(behandlingsresultat.getId())
            .orElseThrow(() -> BehandlingRepositoryFeil.FACTORY.fantIkkeBehandlingVedtak(behandlingId).toException());
        vedtak.setIverksettingStatus(IverksettingStatus.IVERKSATT);

        behandlingVedtakRepository.lagre(vedtak, kontekst.getSkriveLås());
        behandlingVedtakEventPubliserer.fireEvent(vedtak, behandling);

        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        log.info("Har avsluttet behandling: {}", behandlingId); //$NON-NLS-1$

        // TODO (TOPAS): Kunne vi flyttet dette ut i en Event observer (ref BehandlingStatusEvent) Hilsen FC.
        Optional<Behandling> ventendeBehandlingOpt = finnBehandlingSomVenterIverksetting(behandling);
        ventendeBehandlingOpt.ifPresent(ventendeBehandling -> {
            log.info("Fortsetter iverksetting av ventende behandling: {}", ventendeBehandling.getId()); //$NON-NLS-1$
            opprettTaskForProsesserBehandling(ventendeBehandling);
        });
    }

    private void opprettTaskForProsesserBehandling(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);
    }

    private Optional<Behandling> finnBehandlingSomVenterIverksetting(Behandling behandling) {
        Optional<Behandling> resultat = Optional.empty();
        // Finn behandlinger i samme sak
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(behandling.getFagsakId());
        List<Behandling> behandlinger = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer());
        // Hvis bare denne - ingen venter
        if (behandlinger.size() <= 1) {
            return resultat;
        }
        // Finn vedtak i alle behandlinger
        List<Behandling> medVedtak = behandlinger.stream()
            .filter(b -> behandlingRepository.hentResultatHvisEksisterer(b.getId()).isPresent())
            .collect(Collectors.toList());
        // Hvis bare denne behandling har vedtak - ingen venter
        if (medVedtak.size() <= 1) {
            return resultat;
        }
        // Hvis finnes vedtak i status IKKE_IVERKSATT, returner eldste av disse
        Optional<BehandlingVedtak> vedtak = medVedtak.stream()
            .map(b -> behandlingVedtakRepository.hentVedtakFor(behandlingRepository.hentResultat(b.getId()).getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(v -> IverksettingStatus.IKKE_IVERKSATT.equals(v.getIverksettingStatus()))
            .min(Comparator.comparing(BaseEntitet::getOpprettetTidspunkt));
        if (vedtak.isPresent()) {
            resultat = Optional.of(vedtak.get().getBehandlingsresultat().getBehandling());
        }
        return resultat;
    }
}
