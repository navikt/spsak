package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakHistorikkTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakSteg;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task.AvsluttBehandlingTask;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.task.SendVedtaksbrevTask;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.IverksettingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveVurderKonsekvensTask;
import no.nav.foreldrepenger.domene.vedtak.KanVedtaketIverksettesTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@BehandlingStegRef(kode = "IVEDSTEG")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class IverksetteVedtakStegImpl implements IverksetteVedtakSteg {

    private static final Logger log = LoggerFactory.getLogger(IverksetteVedtakStegImpl.class);

    private static final String BESKRIVELSESTEKST = "Vedtaket er opphevet eller omgjort. Opprett en ny behandling.";

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer;
    private OppgaveTjeneste oppgaveTjeneste;
    private IverksetteVedtakHistorikkTjeneste iverksetteVedtakHistorikkTjeneste;
    private KanVedtaketIverksettesTjeneste kanVedtaketIverksettesTjeneste;
    private BehandlingVedtakRepository behandlingVedtakRepository;

    IverksetteVedtakStegImpl() {
        // for CDI proxy
    }

    @Inject
    IverksetteVedtakStegImpl(BehandlingRepositoryProvider repositoryProvider, ProsessTaskRepository prosessTaskRepository,
                             BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer, OppgaveTjeneste oppgaveTjeneste,
                             IverksetteVedtakHistorikkTjeneste iverksetteVedtakHistorikkTjeneste,
                             KanVedtaketIverksettesTjeneste kanVedtaketIverksettesTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.prosessTaskRepository = prosessTaskRepository;
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.behandlingVedtakEventPubliserer = behandlingVedtakEventPubliserer;
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.iverksetteVedtakHistorikkTjeneste = iverksetteVedtakHistorikkTjeneste;
        this.kanVedtaketIverksettesTjeneste = kanVedtaketIverksettesTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Optional<BehandlingVedtak> fantVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId);
        if (!fantVedtak.isPresent()) {
            log.info("Behandling {}: Kan ikke iverksette, behandling mangler vedtak", behandlingId);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        BehandlingVedtak vedtak = fantVedtak.get();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (IverksettingStatus.IKKE_IVERKSATT.equals(vedtak.getIverksettingStatus())) {
            boolean kanIverksettes = true;
            boolean venterTidligereBehandling = iverksettingHindresAvAnnenBehandling(behandling);
            if (gjelderFPOgVedtakResultatInnvilget(behandling, vedtak)) {
                kanIverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);
            }
            if (venterTidligereBehandling || !kanIverksettes) {
                log.info("Behandling {}: Iverksetting venter på annen behandling eller opphør av ytelse i Infotrygd", behandlingId);
                iverksetteVedtakHistorikkTjeneste.opprettHistorikkinnslagNårIverksettelsePåVent(behandling, venterTidligereBehandling, kanIverksettes);
                return venter();
            }
            log.info("Behandling {}: Iverksetter vedtak", behandlingId);
            vedtak.setIverksettingStatus(IverksettingStatus.UNDER_IVERKSETTING);
            behandlingVedtakRepository.lagre(vedtak, kontekst.getSkriveLås());
            behandlingVedtakEventPubliserer.fireEvent(vedtak, behandling);
            opprettIverksettingstasker(behandling);
            return venter();
        } else if (IverksettingStatus.UNDER_IVERKSETTING.equals(vedtak.getIverksettingStatus())) {
            log.info("Behandling {}: Iverksetting fortsatt ikke fullført", behandlingId);
            return venter();
        } else {
            log.info("Behandling {}: Iverksetting fullført", behandlingId);
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
    }

    private boolean gjelderFPOgVedtakResultatInnvilget(Behandling behandling, BehandlingVedtak vedtak) {
        return behandling.getFagsakYtelseType().gjelderForeldrepenger()
            && vedtak.getVedtakResultatType() != null && vedtak.getVedtakResultatType().equals(VedtakResultatType.INNVILGET);
    }

    void opprettIverksettingstasker(Behandling behandling) {
        ProsessTaskGruppe taskData;
        ProsessTaskData avsluttBehandling = new ProsessTaskData(AvsluttBehandlingTask.TASKTYPE);
        Optional<ProsessTaskData> avsluttOppgave = oppgaveTjeneste.opprettTaskAvsluttOppgave(behandling, behandling.getBehandleOppgaveÅrsak(), false);
        ProsessTaskData sendVedtaksbrev = new ProsessTaskData(SendVedtaksbrevTask.TASKTYPE);

        if (behandling.erKlage()) {
            //TODO E149421 vurder å flytte ut til egen implementasjon av steget
            taskData = opprettTaskDataForKlage(behandling, avsluttBehandling, sendVedtaksbrev, avsluttOppgave);
        } else if (behandling.erInnsyn()) {
            //TODO E149421 vurder å flytte til egen implementasjon av steget, gjøre evt. når det sendes ut brev
            taskData = opprettTaskDataForInnsyn(avsluttBehandling, avsluttOppgave);
        } else {
            taskData = new ProsessTaskGruppe();
            if (avsluttOppgave.isPresent()) {
                taskData.addNesteParallell(avsluttOppgave.get());
            }
            taskData.addNesteSekvensiell(avsluttBehandling);
        }
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());

        taskData.setCallIdFraEksisterende();

        prosessTaskRepository.lagre(taskData);
    }

    private ProsessTaskGruppe opprettTaskDataForInnsyn(ProsessTaskData avsluttBehandling, Optional<ProsessTaskData> avsluttOppgave) {
        ProsessTaskGruppe taskData = new ProsessTaskGruppe();
        avsluttOppgave.ifPresent(taskData::addNesteParallell);
        taskData.addNesteSekvensiell(avsluttBehandling);
        return taskData;
    }

    private ProsessTaskGruppe opprettTaskDataForKlage(Behandling behandling, ProsessTaskData avsluttBehandling,
                                                      ProsessTaskData sendVedtaksbrev, Optional<ProsessTaskData> avsluttOppgave) {
        ProsessTaskGruppe taskData = new ProsessTaskGruppe();
        if (avsluttOppgave.isPresent()) {
            taskData.addNesteParallell(sendVedtaksbrev, avsluttOppgave.get());
        } else {
            taskData.addNesteSekvensiell(sendVedtaksbrev);
        }
        taskData.addNesteSekvensiell(avsluttBehandling);
        Optional<KlageVurderingResultat> vurderingsresultat = behandling.hentGjeldendeKlageVurderingResultat();
        if (vurderingsresultat.isPresent()) {
            KlageVurdering vurdering = vurderingsresultat.get().getKlageVurdering();
            if (KlageVurdering.MEDHOLD_I_KLAGE.equals(vurdering) || KlageVurdering.OPPHEVE_YTELSESVEDTAK.equals(vurdering)) {
                Behandling sisteFørstegangsbehandling = behandlingRepository.hentSisteBehandlingForFagsakId(behandling.getFagsakId(),
                    BehandlingType.FØRSTEGANGSSØKNAD).orElse(behandling);
                ProsessTaskData opprettOppgave = new ProsessTaskData(OpprettOppgaveVurderKonsekvensTask.TASKTYPE);
                opprettOppgave.setProperty(OpprettOppgaveVurderKonsekvensTask.KEY_BEHANDLENDE_ENHET, sisteFørstegangsbehandling.getBehandlendeEnhet());
                opprettOppgave.setProperty(OpprettOppgaveVurderKonsekvensTask.KEY_BESKRIVELSE, BESKRIVELSESTEKST);
                opprettOppgave.setProperty(OpprettOppgaveVurderKonsekvensTask.KEY_PRIORITET, OpprettOppgaveVurderKonsekvensTask.PRIORITET_HØY);
                taskData.addNesteSekvensiell(opprettOppgave);
            }
        }
        return taskData;
    }

    private BehandleStegResultat venter() {
        return BehandleStegResultat.settPåVent();
    }

    boolean iverksettingHindresAvAnnenBehandling(Behandling behandling) {
        if (behandling.erInnsyn()) {
            return false; //trenger ikke å vente på andre behandlinger
        }
        // Iverksetting er hindret hvis det finnes annet vedtak i samme sak, og med status UNDER_IVERKSETTING
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(behandling.getFagsakId());
        List<Behandling> behandlinger = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer());
        return behandlinger.stream()
            .map(b -> behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(b.getId()))
            .anyMatch(v -> v.isPresent() && IverksettingStatus.UNDER_IVERKSETTING.equals(v.get().getIverksettingStatus()));
    }

}
